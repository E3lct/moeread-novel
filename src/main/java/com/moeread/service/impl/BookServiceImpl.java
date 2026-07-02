package com.moeread.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moeread.dto.BookVO;
import com.moeread.entity.*;
import com.moeread.mapper.*;
import com.moeread.service.BookService;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Value("${moeread.upload.path}")
    private String uploadPath;

    private final ChapterMapper chapterMapper;
    private final ReadProgressMapper readProgressMapper;
    private final BookTagMapper bookTagMapper;
    private final TagMapper tagMapper;
    private final ReadingStatsMapper readingStatsMapper;

    // 章节标题匹配规则（精简版，完整 Legado 规则后续移植）
    private static final String[] CHAPTER_PATTERNS = {
        "^\\s*第\\s*[0-9零〇一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]+\\s*[章章节回卷部集]\\s*.*$",
        "^\\s*[卷部集]\\s*[0-9零〇一二两三四五六七八九十百千万]+\\s*.*$",
        "^\\s*(正文)?\\s*第\\s*.{1,12}\\s*[章章节回]\\s*.*$",
        "^\\s*Chapter\\s+[0-9IVXLCDM]+\\b.*$",
        "^\\s*[0-9]{1,5}\\s*[.、．]\\s*\\S.*$",
        "^\\s*[序楔尾后番外终][章声篇记]?.*$"
    };
    private static final List<Pattern> CHAPTER_TITLE_PATTERNS = Arrays.stream(CHAPTER_PATTERNS)
            .map(Pattern::compile)
            .toList();

    public BookServiceImpl(ChapterMapper chapterMapper, ReadProgressMapper readProgressMapper,
                           BookTagMapper bookTagMapper, TagMapper tagMapper,
                           ReadingStatsMapper readingStatsMapper) {
        this.chapterMapper = chapterMapper;
        this.readProgressMapper = readProgressMapper;
        this.bookTagMapper = bookTagMapper;
        this.tagMapper = tagMapper;
        this.readingStatsMapper = readingStatsMapper;
    }

    @Override
    public List<BookVO> listBooks(Integer userId) {
        List<Book> books = list(new LambdaQueryWrapper<Book>()
                .eq(Book::getUserId, userId).orderByDesc(Book::getCreateTime));
        return enrichBooks(userId, books);
    }

    @Override
    public List<BookVO> searchBooks(Integer userId, String keyword) {
        List<Book> books = list(new LambdaQueryWrapper<Book>()
                .eq(Book::getUserId, userId)
                .like(Book::getTitle, keyword)
                .orderByDesc(Book::getCreateTime));
        return enrichBooks(userId, books);
    }

    @Override
    @Transactional
    public Book importTxt(Integer userId, MultipartFile file) {
        String title = file.getOriginalFilename();
        if (title != null && title.endsWith(".txt")) {
            title = title.substring(0, title.length() - 4);
        }

        String content;
        try {
            content = decodeText(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage());
        }

        return importTextContent(userId, title, null, content, "txt");
    }

    @Override
    @Transactional
    public List<Book> importZip(Integer userId, MultipartFile file) {
        List<Book> imported = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (!name.toLowerCase().endsWith(".txt")) continue;

                // 去掉路径前缀
                String title = new File(name).getName();
                if (title.endsWith(".txt")) title = title.substring(0, title.length() - 4);

                // 读取内容，处理可能的乱码
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int len;
                while ((len = zis.read(buf)) != -1) bos.write(buf, 0, len);
                String content = decodeText(bos.toByteArray());

                Book book = importTextContent(userId, title, null, content, "zip");
                imported.add(book);
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("读取ZIP失败: " + e.getMessage());
        }
        return imported;
    }

    @Override
    @Transactional
    public Book importTextContent(Integer userId, String title, String author, String content, String sourceType) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("导入内容为空");
        }
        String safeTitle = title != null && !title.trim().isEmpty() ? title.trim() : "未命名书籍";
        Book book = createBook(userId, safeTitle, author, sourceType);
        List<Chapter> chapters = parseChapters(book.getId(), content);
        if (!chapters.isEmpty()) {
            for (Chapter ch : chapters) {
                chapterMapper.insert(ch);
            }
            book.setChapterCount(chapters.size());
            book.setTotalWords(content.length());
            updateById(book);
        }
        return book;
    }

    @Override
    @Transactional
    public void updateBook(Integer userId, Integer bookId, BookVO vo) {
        Book book = getOne(new LambdaQueryWrapper<Book>()
                .eq(Book::getId, bookId).eq(Book::getUserId, userId));
        if (book == null) throw new RuntimeException("书籍不存在");
        if (vo.getTitle() != null) book.setTitle(vo.getTitle());
        if (vo.getAuthor() != null) book.setAuthor(vo.getAuthor());
        if (vo.getDescription() != null) book.setDescription(vo.getDescription());
        if (vo.getCoverImagePath() != null) book.setCoverImagePath(vo.getCoverImagePath());
        if (vo.getCoverColor() != null) book.setCoverColor(vo.getCoverColor());
        if (vo.getStatus() != null) book.setStatus(vo.getStatus());
        if (vo.getIsFavorite() != null) book.setIsFavorite(vo.getIsFavorite());
        updateById(book);
        // 同步标签
        if (vo.getTags() != null) {
            List<BookTag> existing = bookTagMapper.selectList(
                    new LambdaQueryWrapper<BookTag>()
                            .eq(BookTag::getBookId, bookId)
                            .eq(BookTag::getUserId, userId));
            Set<String> currentTags = new HashSet<>();
            for (BookTag bt : existing) currentTags.add(bt.getTagName());
            Set<String> newTags = new HashSet<>(vo.getTags());
            // 添加新标签
            for (String tag : newTags) {
                if (!currentTags.contains(tag)) {
                    BookTag bt = new BookTag();
                    bt.setUserId(userId);
                    bt.setBookId(bookId);
                    bt.setTagName(tag);
                    bt.setCreateTime(LocalDateTime.now());
                    bookTagMapper.insert(bt);
                }
            }
            // 删除旧标签
            for (BookTag bt : existing) {
                if (!newTags.contains(bt.getTagName())) {
                    bookTagMapper.deleteById(bt.getId());
                }
            }
        }
    }

    @Override
    @Transactional
    public void deleteBook(Integer userId, Integer bookId) {
        Book book = getOne(new LambdaQueryWrapper<Book>()
                .eq(Book::getId, bookId).eq(Book::getUserId, userId));
        if (book == null) throw new RuntimeException("书籍不存在");
        // 级联删除
        chapterMapper.delete(new LambdaQueryWrapper<Chapter>().eq(Chapter::getBookId, bookId));
        readProgressMapper.delete(new LambdaQueryWrapper<ReadProgress>().eq(ReadProgress::getBookId, bookId));
        bookTagMapper.delete(new LambdaQueryWrapper<BookTag>().eq(BookTag::getBookId, bookId));
        removeById(bookId);
    }

    @Override
    public String uploadCover(Integer userId, MultipartFile file) {
        try {
            Path dir = Paths.get(uploadPath).toAbsolutePath().resolve("covers");
            Files.createDirectories(dir);
            String ext = ".jpg";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String filename = userId + "_" + System.currentTimeMillis() + ext;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/covers/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("封面上传失败: " + e.getMessage());
        }
    }

    // ---- 私有方法 ----

    private Book createBook(Integer userId, String title, String author, String sourceType) {
        Book book = new Book();
        book.setUserId(userId);
        book.setTitle(title);
        book.setAuthor(author != null ? author : "未知");
        book.setCoverColor(Book.randomCoverColor());
        book.setSourceType(sourceType);
        book.setStatus("reading");
        book.setIsFavorite(0);
        save(book);
        return book;
    }

    /** 解析章节 */
    private List<Chapter> parseChapters(Integer bookId, String content) {
        String normalized = content
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace("\uFEFF", "");
        String[] lines = normalized.split("\n");
        List<Chapter> chapters = new ArrayList<>();

        int startLine = 0;
        String chapterTitle = "前言";
        StringBuilder chapterContent = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() && chapterContent.length() == 0) {
                startLine++;
                continue;
            }

            boolean isTitle = false;
            if (line.length() <= 60) {
                for (Pattern pattern : CHAPTER_TITLE_PATTERNS) {
                    if (pattern.matcher(line).matches()) {
                        isTitle = true;
                        break;
                    }
                }
            }
            if (!isTitle && line.length() <= 40 && chapterContent.length() > 1500) {
                if (line.matches("^\\s*[一二三四五六七八九十百千万]+\\s*$")
                        || line.matches("^\\s*[0-9]{1,4}\\s*$")) {
                    isTitle = true;
                }
            }

            if (isTitle && chapterContent.length() > 0) {
                // 保存上一章
                chapters.add(buildChapter(bookId, chapters.size(), chapterTitle, chapterContent.toString()));
                chapterTitle = line;
                chapterContent = new StringBuilder();
            } else if (isTitle) {
                chapterTitle = line;
                chapterContent = new StringBuilder();
                startLine = i + 1;
            } else {
                if (chapterContent.length() > 0) chapterContent.append("\n");
                chapterContent.append(line);
            }
        }

        // 最后一章
        if (chapterContent.length() > 0 || chapterTitle != null) {
            chapters.add(buildChapter(bookId, chapters.size(), chapterTitle, chapterContent.toString()));
        }

        // 如果没有识别到章节，整本书当一章
        if (chapters.isEmpty() && content.trim().length() > 0) {
            chapters.add(buildChapter(bookId, 0, "第一章", content.trim()));
        }

        return chapters;
    }

    private String decodeText(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xef
                && (bytes[1] & 0xff) == 0xbb
                && (bytes[2] & 0xff) == 0xbf) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String charset = detector.getDetectedCharset();
        detector.reset();
        if (charset == null || charset.isBlank()) {
            charset = looksLikeUtf8(bytes) ? "UTF-8" : "GB18030";
        }
        try {
            return new String(bytes, Charset.forName(charset));
        } catch (Exception e) {
            return new String(bytes, Charset.forName("GB18030"));
        }
    }

    private boolean looksLikeUtf8(byte[] bytes) {
        int i = 0;
        while (i < bytes.length) {
            int b = bytes[i] & 0xff;
            if (b <= 0x7f) {
                i++;
            } else if ((b >> 5) == 0b110 && i + 1 < bytes.length && ((bytes[i + 1] & 0xc0) == 0x80)) {
                i += 2;
            } else if ((b >> 4) == 0b1110 && i + 2 < bytes.length
                    && ((bytes[i + 1] & 0xc0) == 0x80)
                    && ((bytes[i + 2] & 0xc0) == 0x80)) {
                i += 3;
            } else if ((b >> 3) == 0b11110 && i + 3 < bytes.length
                    && ((bytes[i + 1] & 0xc0) == 0x80)
                    && ((bytes[i + 2] & 0xc0) == 0x80)
                    && ((bytes[i + 3] & 0xc0) == 0x80)) {
                i += 4;
            } else {
                return false;
            }
        }
        return true;
    }

    private Chapter buildChapter(Integer bookId, int index, String title, String content) {
        Chapter ch = new Chapter();
        ch.setBookId(bookId);
        ch.setChapterIndex(index);
        ch.setTitle(title.trim());
        ch.setContent(content);
        ch.setWordCount(content.length());
        return ch;
    }

    /** 丰富书籍信息：进度 + 标签 */
    private List<BookVO> enrichBooks(Integer userId, List<Book> books) {
        List<BookVO> vos = new ArrayList<>();
        for (Book book : books) {
            BookVO vo = new BookVO();
            BeanUtils.copyProperties(book, vo);

            // 阅读进度
            ReadProgress progress = readProgressMapper.selectOne(
                    new LambdaQueryWrapper<ReadProgress>()
                            .eq(ReadProgress::getBookId, book.getId())
                            .eq(ReadProgress::getUserId, userId));
            if (progress != null) {
                vo.setReadProgress(progress.getScrollPercent());
                vo.setCurrentChapter(progress.getChapterIndex());
            } else {
                vo.setReadProgress(0);
                vo.setCurrentChapter(0);
            }

            // 标签
            List<BookTag> btList = bookTagMapper.selectList(
                    new LambdaQueryWrapper<BookTag>().eq(BookTag::getBookId, book.getId()));
            List<String> tags = new ArrayList<>();
            for (BookTag bt : btList) {
                tags.add(bt.getTagName());
            }
            vo.setTags(tags);

            vos.add(vo);
        }
        return vos;
    }
}

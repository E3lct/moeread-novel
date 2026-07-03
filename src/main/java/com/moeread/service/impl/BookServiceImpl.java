package com.moeread.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moeread.dto.BookVO;
import com.moeread.entity.*;
import com.moeread.mapper.*;
import com.moeread.service.BookService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
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
import java.util.regex.Matcher;
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
        "^\\s*(前言|序言|序章|楔子|引子|正文|后记|尾声|终章|番外)(\\s*[:：].*)?$",
        "^\\s*[序楔尾终][章声篇记]\\s*.*$",
        "^\\s*(第\\s*)?[一二三四五六七八九十百千万0-9]{1,6}\\s*[节话篇]\\s*.*$"
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
    public Book importEpub(Integer userId, MultipartFile file) {
        byte[] epubBytes;
        try {
            epubBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取 EPUB 失败: " + e.getMessage());
        }
        String fallbackTitle = cleanField(file.getOriginalFilename(), 200, "未命名书籍");
        if (fallbackTitle.toLowerCase().endsWith(".epub")) fallbackTitle = fallbackTitle.substring(0, fallbackTitle.length() - 5);
        Map<String, byte[]> entries = readEpubEntries(epubBytes);
        String containerXml = textEntry(entries, "META-INF/container.xml");
        String title = fallbackTitle;
        String author = null;
        Map<String, ManifestItem> manifest = new LinkedHashMap<>();
        List<ManifestItem> spine = new ArrayList<>();
        String opfXml = "";

        if (containerXml != null && !containerXml.isBlank()) {
            Document container = Jsoup.parse(containerXml, "", Parser.xmlParser());
            Element rootfile = container.selectFirst("rootfile[full-path]");
            if (rootfile != null) {
                String opfPath = normalizeZipPath(rootfile.attr("full-path"));
                opfXml = textEntry(entries, opfPath);
                if (opfXml != null && !opfXml.isBlank()) {
                    String opfDir = parentDir(opfPath);
                    String parsedTitle = firstTagText(opfXml, "dc:title", "title");
                    if (parsedTitle != null && !parsedTitle.isBlank()) title = parsedTitle;
                    author = firstTagText(opfXml, "dc:creator", "creator");
                    manifest = parseManifest(opfXml, opfDir);
                    spine = parseSpine(opfXml, manifest, entries);
                }
            }
        }

        Book book = createBook(userId, title, author, "epub");
        saveOriginalEpub(epubBytes, userId, book.getId());
        Map<String, String> assetMap = exportEpubAssets(entries, userId, book.getId());
        String cover = findEpubCover(opfXml, manifest, assetMap);
        if (cover != null) book.setCoverImagePath(cover);

        List<Chapter> chapters = new ArrayList<>();
        for (ManifestItem item : spine) {
            String xhtml = textEntry(entries, item.path);
            String html = sanitizeEpubChapter(xhtml, item.path, assetMap, entries, userId, book.getId());
            if (html == null || html.isBlank()) continue;
            String chapterTitle = chapterTitleFromHtml(xhtml);
            if (chapterTitle == null || chapterTitle.isBlank()) chapterTitle = "第" + (chapters.size() + 1) + "章";
            chapters.add(buildChapter(book.getId(), chapters.size(), cleanField(chapterTitle, 120, "章节"), html));
        }
        if (chapters.isEmpty()) {
            chapters.add(buildChapter(book.getId(), 0, cleanField(title, 120, "EPUB"), "这本 EPUB 将使用原文件阅读器打开。"));
        }
        if ((book.getCoverImagePath() == null || book.getCoverImagePath().isBlank()) && !assetMap.isEmpty()) {
            book.setCoverImagePath(assetMap.values().iterator().next());
        }
        for (Chapter chapter : chapters) chapterMapper.insert(chapter);
        book.setChapterCount(chapters.size());
        book.setTotalWords(chapters.stream().mapToInt(ch -> ch.getWordCount() == null ? 0 : ch.getWordCount()).sum());
        updateById(book);
        return book;
    }

    @Override
    @Transactional
    public Book importTextContent(Integer userId, String title, String author, String content, String sourceType) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("导入内容为空");
        }
        String safeTitle = cleanField(title, 200, "未命名书籍");
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
        book.setTitle(cleanField(title, 200, "未命名书籍"));
        book.setAuthor(cleanAuthor(author));
        book.setCoverColor(Book.randomCoverColor());
        book.setSourceType(cleanField(sourceType, 120, "import"));
        book.setStatus("reading");
        book.setIsFavorite(0);
        save(book);
        return book;
    }

    private String cleanAuthor(String author) {
        String value = cleanField(author, 120, "未知");
        if (value.length() > 40 && value.matches(".*(简介|推荐度|连载|完结|章节|万字|第\\s*\\d+|。|，).*")) {
            return "未知";
        }
        return value;
    }

    private String cleanField(String value, int maxLength, String fallback) {
        String cleaned = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) return fallback;
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }

    private void saveOriginalEpub(byte[] bytes, Integer userId, Integer bookId) {
        try {
            Path root = Paths.get(uploadPath).toAbsolutePath().resolve("epub").resolve(userId + "_" + bookId);
            Files.createDirectories(root);
            Files.write(root.resolve("book.epub"), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("保存 EPUB 原文件失败: " + e.getMessage());
        }
    }

    private Map<String, byte[]> readEpubEntries(byte[] bytes) {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        long total = 0;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = normalizeZipPath(entry.getName());
                if (name.isBlank() || name.contains("..")) continue;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int len;
                while ((len = zis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                    total += len;
                    if (total > 120L * 1024 * 1024) {
                        throw new RuntimeException("EPUB 文件过大，请拆分后导入");
                    }
                }
                entries.put(name, bos.toByteArray());
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("读取 EPUB 失败: " + e.getMessage());
        }
        return entries;
    }

    private String textEntry(Map<String, byte[]> entries, String path) {
        byte[] bytes = entries.get(normalizeZipPath(path));
        return bytes == null ? null : decodeText(bytes);
    }

    private String normalizeZipPath(String path) {
        if (path == null) return "";
        return path.replace('\\', '/').replaceAll("^/+", "").trim();
    }

    private String parentDir(String path) {
        String normalized = normalizeZipPath(path);
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(0, index + 1) : "";
    }

    private String resolveEpubPath(String baseDir, String href) {
        String raw = normalizeZipPath(href == null ? "" : href.split("#", 2)[0]);
        String combined = normalizeZipPath(baseDir) + "/" + raw;
        Deque<String> parts = new ArrayDeque<>();
        for (String part : combined.split("/+")) {
            if (part.isBlank() || ".".equals(part)) continue;
            if ("..".equals(part)) {
                if (!parts.isEmpty()) parts.removeLast();
            } else {
                parts.addLast(part);
            }
        }
        return String.join("/", parts);
    }

    private Map<String, ManifestItem> parseManifest(String opfXml, String opfDir) {
        Map<String, ManifestItem> manifest = new LinkedHashMap<>();
        Matcher matcher = Pattern.compile("<\\s*item\\b([^>]*)>", Pattern.CASE_INSENSITIVE).matcher(opfXml);
        while (matcher.find()) {
            String attrs = matcher.group(1);
            String id = xmlAttr(attrs, "id");
            String href = xmlAttr(attrs, "href");
            if (id == null || href == null) continue;
            ManifestItem mi = new ManifestItem();
            mi.id = id;
            mi.href = href;
            mi.mediaType = xmlAttr(attrs, "media-type");
            mi.properties = xmlAttr(attrs, "properties");
            mi.path = resolveEpubPath(opfDir, href);
            manifest.put(id, mi);
        }
        return manifest;
    }

    private List<ManifestItem> parseSpine(String opfXml, Map<String, ManifestItem> manifest, Map<String, byte[]> entries) {
        List<ManifestItem> spine = new ArrayList<>();
        Matcher matcher = Pattern.compile("<\\s*itemref\\b([^>]*)>", Pattern.CASE_INSENSITIVE).matcher(opfXml);
        while (matcher.find()) {
            String idref = xmlAttr(matcher.group(1), "idref");
            ManifestItem mi = idref == null ? null : manifest.get(idref);
            if (mi != null && entries.containsKey(mi.path)) spine.add(mi);
        }
        return spine;
    }

    private String xmlAttr(String attrs, String name) {
        if (attrs == null) return null;
        Matcher matcher = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*=\\s*(['\"])(.*?)\\1", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
                .matcher(attrs);
        return matcher.find() ? Jsoup.parse(matcher.group(2)).text().trim() : null;
    }

    private String firstTagText(String xml, String... tagNames) {
        if (xml == null) return null;
        for (String tag : tagNames) {
            Matcher matcher = Pattern.compile("<\\s*" + Pattern.quote(tag) + "\\b[^>]*>(.*?)</\\s*" + Pattern.quote(tag) + "\\s*>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(xml);
            if (matcher.find()) return Jsoup.parse(matcher.group(1)).text().trim();
        }
        return null;
    }

    private Map<String, String> exportEpubAssets(Map<String, byte[]> entries, Integer userId, Integer bookId) {
        Map<String, String> assetMap = new HashMap<>();
        Path root = Paths.get(uploadPath).toAbsolutePath().resolve("epub").resolve(userId + "_" + bookId);
        try {
            Files.createDirectories(root);
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                if (!isEpubImage(entry.getKey(), null) || assetMap.containsKey(entry.getKey())) continue;
                saveEpubAsset(assetMap, root, userId, bookId, entry.getKey(), null, entry.getValue());
            }
        } catch (IOException e) {
            throw new RuntimeException("保存 EPUB 插图失败: " + e.getMessage());
        }
        return assetMap;
    }

    private void saveEpubAsset(Map<String, String> assetMap, Path root, Integer userId, Integer bookId,
                               String path, String mediaType, byte[] bytes) throws IOException {
        String ext = imageExtension(path, mediaType);
        String filename = safeAssetName(path, ext);
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root)) return;
        Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        assetMap.put(path, "/uploads/epub/" + userId + "_" + bookId + "/" + filename);
    }

    private boolean isEpubImage(String path, String mediaType) {
        String type = mediaType == null ? "" : mediaType.toLowerCase();
        String lower = path == null ? "" : path.toLowerCase();
        return type.startsWith("image/")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".webp")
                || lower.endsWith(".gif")
                || lower.endsWith(".svg");
    }

    private String imageExtension(String path, String mediaType) {
        String lower = path == null ? "" : path.toLowerCase();
        int dot = lower.lastIndexOf('.');
        if (dot >= 0 && dot > lower.lastIndexOf('/')) return lower.substring(dot);
        if (mediaType != null && mediaType.contains("png")) return ".png";
        if (mediaType != null && mediaType.contains("webp")) return ".webp";
        if (mediaType != null && mediaType.contains("gif")) return ".gif";
        if (mediaType != null && mediaType.contains("svg")) return ".svg";
        return ".jpg";
    }

    private String safeAssetName(String path, String ext) {
        String normalized = normalizeZipPath(path).replace('/', '_').replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!normalized.toLowerCase().endsWith(ext)) normalized += ext;
        return normalized.length() <= 160 ? normalized : Math.abs(path.hashCode()) + ext;
    }

    private String findEpubCover(String opfXml, Map<String, ManifestItem> manifest, Map<String, String> assetMap) {
        Matcher meta = Pattern.compile("<\\s*meta\\b([^>]*)>", Pattern.CASE_INSENSITIVE).matcher(opfXml);
        while (meta.find()) {
            String attrs = meta.group(1);
            if (!"cover".equalsIgnoreCase(xmlAttr(attrs, "name"))) continue;
            ManifestItem item = manifest.get(xmlAttr(attrs, "content"));
            if (item != null && assetMap.containsKey(item.path)) return assetMap.get(item.path);
        }
        for (ManifestItem item : manifest.values()) {
            String properties = item.properties == null ? "" : item.properties;
            if (properties.contains("cover-image") && assetMap.containsKey(item.path)) return assetMap.get(item.path);
        }
        return null;
    }

    private String sanitizeEpubChapter(String xhtml, String chapterPath, Map<String, String> assetMap,
                                       Map<String, byte[]> entries, Integer userId, Integer bookId) {
        if (xhtml == null || xhtml.isBlank()) return null;
        Document document = Jsoup.parse(xhtml);
        document.select("script, style, iframe, object, embed, form, input, button, textarea, select, link, meta").remove();
        Element body = document.body();
        if (body == null) return null;
        String baseDir = parentDir(chapterPath);
        Path assetRoot = Paths.get(uploadPath).toAbsolutePath().resolve("epub").resolve(userId + "_" + bookId);
        for (Element element : body.select("*")) {
            List<String> removeAttrs = new ArrayList<>();
            element.attributes().forEach(attr -> {
                String key = attr.getKey().toLowerCase();
                if (key.startsWith("on") || key.equals("style") || key.equals("srcset")) removeAttrs.add(attr.getKey());
            });
            for (String attr : removeAttrs) element.removeAttr(attr);
        }
        for (Element image : body.select("img, image")) {
            String src = image.hasAttr("src") ? image.attr("src") : image.attr("xlink:href");
            String imagePath = resolveEpubPath(baseDir, src);
            String resolved = assetMap.get(imagePath);
            if (resolved == null && (!entries.containsKey(imagePath) || !isEpubImage(imagePath, null))) {
                imagePath = findEpubEntryBySuffix(entries, normalizeZipPath(src));
            }
            if (resolved == null && imagePath != null && entries.containsKey(imagePath) && isEpubImage(imagePath, null)) {
                try {
                    Files.createDirectories(assetRoot);
                    saveEpubAsset(assetMap, assetRoot, userId, bookId, imagePath, null, entries.get(imagePath));
                    resolved = assetMap.get(imagePath);
                } catch (IOException ignored) {
                    resolved = null;
                }
            }
            if (resolved == null && imagePath != null && entries.containsKey(imagePath) && isEpubImage(imagePath, null)) {
                resolved = imageDataUri(imagePath, entries.get(imagePath));
            }
            if (resolved == null) {
                image.remove();
                continue;
            }
            if (!"img".equals(image.tagName())) image.tagName("img");
            image.attr("src", resolved);
            image.removeAttr("xlink:href");
            image.attr("loading", "lazy");
        }
        for (Element link : body.select("a[href]")) {
            link.removeAttr("href");
        }
        body.select("img").addClass("reader-illustration");
        String html = body.html().trim();
        return html.isBlank() ? null : html;
    }

    private String imageDataUri(String path, byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > 2 * 1024 * 1024) return null;
        String lower = path == null ? "" : path.toLowerCase();
        String mime = "image/jpeg";
        if (lower.endsWith(".png")) mime = "image/png";
        else if (lower.endsWith(".webp")) mime = "image/webp";
        else if (lower.endsWith(".gif")) mime = "image/gif";
        else if (lower.endsWith(".svg")) mime = "image/svg+xml";
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    private String findEpubEntryBySuffix(Map<String, byte[]> entries, String suffix) {
        if (suffix == null || suffix.isBlank()) return null;
        String normalized = normalizeZipPath(suffix);
        for (String key : entries.keySet()) {
            if (key.equals(normalized) || key.endsWith("/" + normalized)) return key;
        }
        String name = normalized.substring(normalized.lastIndexOf('/') + 1);
        if (name.isBlank()) return null;
        for (String key : entries.keySet()) {
            if (key.endsWith("/" + name) || key.equals(name)) return key;
        }
        return null;
    }

    private String chapterTitleFromHtml(String xhtml) {
        if (xhtml == null) return null;
        Document document = Jsoup.parse(xhtml);
        Element title = document.selectFirst("h1, h2, h3, title");
        return title == null ? null : title.text().trim();
    }

    /** 解析章节 */
    private List<Chapter> parseChapters(Integer bookId, String content) {
        String normalized = content
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace("\uFEFF", "");
        normalized = normalized.replaceAll("(?<!^)(?<!\\n)(第\\s*[0-9零〇一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,8}\\s*[章章节回卷部集]\\s*[^\\n]{0,40})", "\n$1");
        normalized = normalized.replaceAll("(?<!^)(?<!\\n)(前言|序言|序章|楔子|引子|正文|后记|尾声|终章|番外)(\\s*[:：])?(?=\\s*\\n)", "\n$1$2");
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
        ch.setWordCount(Jsoup.parse(content).text().length());
        return ch;
    }

    private static class ManifestItem {
        String id;
        String href;
        String path;
        String mediaType;
        String properties;
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
            if ("epub".equalsIgnoreCase(book.getSourceType())) {
                vo.setEpubUrl("/uploads/epub/" + userId + "_" + book.getId() + "/book.epub");
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

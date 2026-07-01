package com.moeread.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moeread.dto.BookSourceDTO;
import com.moeread.dto.SourceImportDTO;
import com.moeread.entity.Book;
import com.moeread.entity.BookSource;
import com.moeread.mapper.BookSourceMapper;
import com.moeread.service.BookService;
import com.moeread.service.BookSourceService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookSourceServiceImpl extends ServiceImpl<BookSourceMapper, BookSource>
        implements BookSourceService {

    private static final List<BookSourceDTO> PRESETS = List.of(
            preset("gutenberg", "Project Gutenberg", "public_txt",
                    "https://www.gutenberg.org", "公共版权英文图书，可导入 Plain Text UTF-8 地址。", "en"),
            preset("standard-ebooks", "Standard Ebooks", "public_catalog",
                    "https://standardebooks.org", "高质量排版的公共版权英文书籍目录，可保存为书源。", "en"),
            preset("aozora", "青空文库", "public_txt",
                    "https://www.aozora.gr.jp", "日本公共版权文学，可导入公开文本地址。", "ja"),
            preset("custom-opds", "自定义 OPDS/公开目录", "custom",
                    "", "适合自己维护的公开目录、校园资源或个人书库。", "custom")
    );

    private final BookService bookService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public BookSourceServiceImpl(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public List<BookSourceDTO> listPresets() {
        return new ArrayList<>(PRESETS);
    }

    @Override
    public List<BookSourceDTO> listUserSources(Integer userId) {
        List<BookSource> sources = list(new LambdaQueryWrapper<BookSource>()
                .eq(BookSource::getUserId, userId)
                .orderByDesc(BookSource::getEnabled)
                .orderByDesc(BookSource::getCreateTime));
        List<BookSourceDTO> result = new ArrayList<>();
        for (BookSource source : sources) {
            result.add(toDto(source));
        }
        return result;
    }

    @Override
    public BookSourceDTO addSource(Integer userId, BookSourceDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("书源名称不能为空");
        }
        BookSource source = new BookSource();
        source.setUserId(userId);
        source.setName(dto.getName().trim());
        source.setSourceKey(dto.getSourceKey() != null ? dto.getSourceKey().trim() : "custom");
        source.setSourceType(dto.getSourceType() != null ? dto.getSourceType() : "custom");
        source.setBaseUrl(dto.getBaseUrl() != null ? dto.getBaseUrl().trim() : "");
        source.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        source.setLanguage(dto.getLanguage() != null ? dto.getLanguage().trim() : "custom");
        source.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
        source.setIsPreset(dto.getIsPreset() != null ? dto.getIsPreset() : 0);
        source.setCreateTime(LocalDateTime.now());
        save(source);
        return toDto(source);
    }

    @Override
    public BookSourceDTO addPreset(Integer userId, String sourceKey) {
        BookSourceDTO preset = PRESETS.stream()
                .filter(p -> p.getSourceKey().equals(sourceKey))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("预设书源不存在"));

        BookSource existing = getOne(new LambdaQueryWrapper<BookSource>()
                .eq(BookSource::getUserId, userId)
                .eq(BookSource::getSourceKey, sourceKey)
                .last("limit 1"));
        if (existing != null) {
            existing.setEnabled(1);
            updateById(existing);
            return toDto(existing);
        }
        BookSourceDTO dto = new BookSourceDTO();
        BeanUtils.copyProperties(preset, dto);
        dto.setIsPreset(1);
        dto.setEnabled(1);
        return addSource(userId, dto);
    }

    @Override
    public void updateEnabled(Integer userId, Integer sourceId, boolean enabled) {
        BookSource source = getUserSource(userId, sourceId);
        source.setEnabled(enabled ? 1 : 0);
        updateById(source);
    }

    @Override
    public void deleteSource(Integer userId, Integer sourceId) {
        getUserSource(userId, sourceId);
        removeById(sourceId);
    }

    @Override
    @Transactional
    public Book importFromSource(Integer userId, SourceImportDTO dto) {
        BookSource source = getUserSource(userId, dto.getSourceId());
        if (source.getEnabled() == null || source.getEnabled() == 0) {
            throw new RuntimeException("请先启用该书源");
        }
        String url = dto.getContentUrl();
        if (url == null || url.trim().isEmpty()) {
            throw new RuntimeException("公开文本地址不能为空");
        }
        URI uri = validateHttpUrl(url.trim());
        String content = fetchText(uri);
        String title = dto.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = deriveTitle(uri);
        }
        return bookService.importTextContent(userId, title, dto.getAuthor(), content, "source:" + source.getSourceKey());
    }

    private BookSource getUserSource(Integer userId, Integer sourceId) {
        BookSource source = getOne(new LambdaQueryWrapper<BookSource>()
                .eq(BookSource::getId, sourceId)
                .eq(BookSource::getUserId, userId));
        if (source == null) throw new RuntimeException("书源不存在");
        return source;
    }

    private String fetchText(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "Moeread/2.0")
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("书源返回异常: HTTP " + response.statusCode());
            }
            byte[] bytes = response.body();
            if (bytes.length > 20 * 1024 * 1024) {
                throw new RuntimeException("文本过大，请下载后使用本地导入");
            }
            return decodeText(bytes);
        } catch (IOException e) {
            throw new RuntimeException("无法连接书源: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("书源请求被中断");
        }
    }

    private String decodeText(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xef
                && (bytes[1] & 0xff) == 0xbb
                && (bytes[2] & 0xff) == 0xbf) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (utf8.indexOf('\uFFFD') >= 0) {
            return new String(bytes, Charset.forName("GB18030"));
        }
        return utf8;
    }

    private URI validateHttpUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new RuntimeException("仅支持 http/https 地址");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new RuntimeException("文本地址不完整");
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("文本地址格式不正确");
        }
    }

    private String deriveTitle(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) return "书源导入";
        String name = path.substring(path.lastIndexOf('/') + 1);
        if (name.endsWith(".txt")) name = name.substring(0, name.length() - 4);
        return name.isBlank() ? "书源导入" : name;
    }

    private BookSourceDTO toDto(BookSource source) {
        BookSourceDTO dto = new BookSourceDTO();
        dto.setSourceId(source.getId());
        dto.setName(source.getName());
        dto.setSourceKey(source.getSourceKey());
        dto.setSourceType(source.getSourceType());
        dto.setBaseUrl(source.getBaseUrl());
        dto.setDescription(source.getDescription());
        dto.setLanguage(source.getLanguage());
        dto.setEnabled(source.getEnabled());
        dto.setIsPreset(source.getIsPreset());
        return dto;
    }

    private static BookSourceDTO preset(String key, String name, String type, String baseUrl,
                                        String description, String language) {
        BookSourceDTO dto = new BookSourceDTO();
        dto.setSourceKey(key);
        dto.setName(name);
        dto.setSourceType(type);
        dto.setBaseUrl(baseUrl);
        dto.setDescription(description);
        dto.setLanguage(language);
        dto.setEnabled(0);
        dto.setIsPreset(1);
        return dto;
    }
}

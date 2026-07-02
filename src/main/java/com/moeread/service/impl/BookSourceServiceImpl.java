package com.moeread.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeread.dto.BookSourceDTO;
import com.moeread.dto.SourceBookVO;
import com.moeread.dto.SourceImportDTO;
import com.moeread.entity.Book;
import com.moeread.entity.BookSource;
import com.moeread.mapper.BookSourceMapper;
import com.moeread.service.BookService;
import com.moeread.service.BookSourceService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BookSourceServiceImpl extends ServiceImpl<BookSourceMapper, BookSource>
        implements BookSourceService {

    private static final List<BookSourceDTO> PRESETS = List.of(
            preset("gutenberg", "Project Gutenberg", "public_txt",
                    "https://www.gutenberg.org", "", "", "公共版权英文图书，可导入 Plain Text UTF-8 地址。", "en"),
            preset("standard-ebooks", "Standard Ebooks", "public_catalog",
                    "https://standardebooks.org", "", "", "高质量排版的公共版权英文书籍目录，可保存为书源。", "en"),
            preset("aozora", "青空文库", "public_txt",
                    "https://www.aozora.gr.jp", "", "", "日本公共版权文学，可导入公开文本地址。", "ja"),
            preset("custom-json", "自定义 JSON 书源", "json_api",
                    "", "", "", "支持搜索接口返回 JSON 数组，字段含 title/author/contentUrl。", "custom")
    );

    private static final List<Map<String, String>> SUBSCRIPTIONS = List.of(
            Map.of(
                    "name", "XIU2/Yuedu 精品书源 Raw",
                    "url", "https://bitbucket.org/xiu2/yuedu/raw/master/shuyuan",
                    "homepage", "https://github.com/XIU2/Yuedu",
                    "note", "README 提供的网络导入地址，包含阅读/Legado 规则型书源。"
            ),
            Map.of(
                    "name", "XIU2/Yuedu CDN 分流",
                    "url", "https://cdn.jsdmirror.com/gh/XIU2/Yuedu/shuyuan",
                    "homepage", "https://github.com/XIU2/Yuedu",
                    "note", "同一书源的 CDN 分流地址，Raw 不通时可试这个。"
            ),
            Map.of(
                    "name", "Yiove 书源仓库 一键导入",
                    "url", "https://shuyuan-api.yiove.com/import/book-sources/1-100",
                    "homepage", "https://shuyuan.yiove.com",
                    "note", "直接导入 Yiove 当前前 100 个书源，和阅读 APP 的一键导入使用同类接口。"
            ),
            Map.of(
                    "name", "源仓库 YckCeo",
                    "url", "https://www.yckceo.com",
                    "homepage", "https://www.yckceo.com",
                    "note", "书源发布/导航站，适合发现更多来源，导入后作为入口保存。"
            )
    );

    private final BookService bookService;
    private final ObjectMapper objectMapper = new ObjectMapper();
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
    public List<Map<String, String>> listSubscriptions() {
        return SUBSCRIPTIONS;
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
        String defaultKey = "custom-" + Math.abs((dto.getName() + "-" + dto.getBaseUrl() + "-" + System.nanoTime()).hashCode());
        source.setSourceKey(dto.getSourceKey() != null ? dto.getSourceKey().trim() : defaultKey);
        String sourceType = dto.getSourceType();
        if (sourceType == null || sourceType.isBlank()) {
            sourceType = dto.getSearchUrl() != null && !dto.getSearchUrl().isBlank() ? "json_api" : "rule_source";
        }
        source.setSourceType(sourceType);
        source.setBaseUrl(dto.getBaseUrl() != null ? dto.getBaseUrl().trim() : "");
        source.setSearchUrl(dto.getSearchUrl() != null ? dto.getSearchUrl().trim() : "");
        source.setContentUrlTemplate(dto.getContentUrlTemplate() != null ? dto.getContentUrlTemplate().trim() : "");
        source.setRuleConfig(dto.getRuleConfig() != null ? dto.getRuleConfig().trim() : "");
        source.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        source.setLanguage(dto.getLanguage() != null ? dto.getLanguage().trim() : "custom");
        source.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
        source.setIsPreset(dto.getIsPreset() != null ? dto.getIsPreset() : 0);
        source.setCreateTime(LocalDateTime.now());
        save(source);
        return toDto(source);
    }

    @Override
    public List<BookSourceDTO> addSources(Integer userId, List<BookSourceDTO> sources) {
        List<BookSourceDTO> result = new ArrayList<>();
        if (sources == null) return result;
        for (BookSourceDTO source : sources) {
            result.add(addSource(userId, source));
        }
        return result;
    }

    @Override
    public List<BookSourceDTO> subscribeSources(Integer userId, String url) {
        if (url == null || url.isBlank()) {
            throw new RuntimeException("订阅地址不能为空");
        }
        String subscribeUrl = normalizeSubscribeUrl(url.trim());
        String body = fetchText(validateHttpUrl(subscribeUrl));
        if (body.trim().startsWith("<")) {
            String extractedUrl = extractSubscriptionUrl(body);
            if (extractedUrl != null) {
                subscribeUrl = extractedUrl;
                body = fetchText(validateHttpUrl(subscribeUrl));
            } else {
                return addSources(userId, List.of(sourceFromWebEntry(subscribeUrl, body)));
            }
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode listNode = findResultList(root);
            if (listNode == null || !listNode.isArray()) {
                throw new RuntimeException("没有找到可导入的书源列表");
            }
            List<BookSourceDTO> sources = new ArrayList<>();
            int index = 0;
            for (JsonNode item : listNode) {
                BookSourceDTO dto = normalizeSource(item, subscribeUrl, index++);
                if (dto != null) sources.add(dto);
            }
            if (sources.isEmpty()) {
                throw new RuntimeException("订阅中没有可识别书源");
            }
            return addSources(userId, sources);
        } catch (IOException e) {
            throw new RuntimeException("订阅内容不是有效 JSON");
        }
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
        String content = fetchSourceContent(source, uri);
        String title = dto.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = deriveTitle(uri);
        }
        String coverUrl = dto.getCoverUrl();
        if ((coverUrl == null || coverUrl.isBlank()) && "rule_source".equals(source.getSourceType())) {
            coverUrl = fetchSourceCover(source, uri);
        }
        Book book = bookService.importTextContent(userId, title, dto.getAuthor(), content, "source:" + source.getSourceKey());
        if (coverUrl != null && !coverUrl.isBlank()) {
            book.setCoverImagePath(coverUrl.trim());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            book.setDescription(dto.getDescription().trim());
        }
        if ((book.getCoverImagePath() != null && !book.getCoverImagePath().isBlank())
                || (book.getDescription() != null && !book.getDescription().isBlank())) {
            bookService.updateById(book);
        }
        return book;
    }

    private String fetchSourceCover(BookSource source, URI uri) {
        if (source.getRuleConfig() == null || source.getRuleConfig().isBlank()) return null;
        try {
            JsonNode rule = objectMapper.readTree(source.getRuleConfig());
            String body = fetchText(uri);
            if (!body.trim().startsWith("<")) return null;
            Document document = Jsoup.parse(body, uri.toString());
            String cover = extractRule(document, nestedText(rule, "ruleBookInfo", "coverUrl", "cover"));
            if (cover == null || cover.isBlank()) cover = findImageUrl(document);
            return resolveUrl(source, cover);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    @Override
    public List<SourceBookVO> search(Integer userId, String keyword, Integer sourceId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new RuntimeException("请输入搜索关键词");
        }
        List<BookSource> targets;
        if (sourceId != null && sourceId > 0) {
            targets = List.of(getUserSource(userId, sourceId));
        } else {
            targets = list(new LambdaQueryWrapper<BookSource>()
                    .eq(BookSource::getUserId, userId)
                    .eq(BookSource::getEnabled, 1));
            targets = targets.stream()
                    .filter(this::isSearchCandidate)
                    .sorted((left, right) -> Integer.compare(searchPriority(right), searchPriority(left)))
                    .limit(8)
                    .toList();
        }

        List<SourceBookVO> result = new ArrayList<>();
        for (BookSource source : targets) {
            try {
                if ("rule_source".equals(source.getSourceType())) {
                    result.addAll(searchRuleSource(source, keyword.trim()));
                } else {
                    if (source.getSearchUrl() == null || source.getSearchUrl().isBlank()) continue;
                    result.addAll(searchOneSource(source, keyword.trim()));
                }
                if (result.size() >= 40) break;
            } catch (RuntimeException ignored) {
                // 一个书源失败时继续搜索其他书源
            }
        }
        return result;
    }

    private List<SourceBookVO> searchOneSource(BookSource source, String keyword) {
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = source.getSearchUrl()
                .replace("{keyword}", encoded)
                .replace("{key}", encoded);
        String body = fetchText(validateHttpUrl(url));
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode listNode = findResultList(root);
            List<SourceBookVO> result = new ArrayList<>();
            if (listNode != null && listNode.isArray()) {
                for (JsonNode item : listNode) {
                    SourceBookVO vo = new SourceBookVO();
                    vo.setSourceId(source.getId());
                    vo.setSourceName(source.getName());
                    vo.setTitle(text(item, "title", "name", "bookName"));
                    vo.setAuthor(text(item, "author", "writer"));
                    vo.setDescription(text(item, "description", "intro", "desc"));
                    vo.setCoverUrl(resolveUrl(source, text(item, "coverUrl", "cover", "image")));
                    String contentUrl = text(item, "contentUrl", "downloadUrl", "txtUrl", "url");
                    if ((contentUrl == null || contentUrl.isBlank())
                            && source.getContentUrlTemplate() != null
                            && !source.getContentUrlTemplate().isBlank()) {
                        String id = text(item, "id", "bookId", "key");
                        if (id != null) contentUrl = source.getContentUrlTemplate().replace("{id}", id);
                    }
                    vo.setContentUrl(resolveUrl(source, contentUrl));
                    sanitizeBookResult(vo, keyword);
                    if (vo.getTitle() != null && vo.getContentUrl() != null && matchesKeyword(vo, keyword)) {
                        result.add(vo);
                    }
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("书源搜索结果不是可识别 JSON");
        }
    }

    private List<SourceBookVO> searchRuleSource(BookSource source, String keyword) {
        if (source.getRuleConfig() == null || source.getRuleConfig().isBlank()) return List.of();
        try {
            JsonNode rule = objectMapper.readTree(source.getRuleConfig());
            String searchUrl = firstText(rule, "ruleSearchUrl", "searchUrl");
            if (searchUrl == null || searchUrl.isBlank()) searchUrl = nestedText(rule, "ruleSearch", "searchUrl", "url");
            if (searchUrl == null || searchUrl.isBlank()) searchUrl = source.getSearchUrl();
            if (isUnsupportedSearchUrl(searchUrl)) return List.of();
            if (searchUrl == null || searchUrl.isBlank()) return List.of();
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = cleanLegadoUrl(searchUrl)
                    .replace("{{key}}", encoded)
                    .replace("{{keyword}}", encoded)
                    .replace("{{page}}", "1")
                    .replace("{keyword}", encoded)
                    .replace("{key}", encoded)
                    .replace("{page}", "1");
            url = resolveUrl(source, url);
            String html = fetchText(validateHttpUrl(url));
            Document document = Jsoup.parse(html, url);
            String listRule = firstText(rule, "ruleSearchList", "searchList", "bookList");
            if (listRule == null || listRule.isBlank()) listRule = nestedText(rule, "ruleSearch", "bookList", "list");
            Elements items = selectElements(document, listRule);
            if (items.isEmpty()) items = document.select("a[href]");
            List<SourceBookVO> result = new ArrayList<>();
            int limit = Math.min(items.size(), 60);
            for (int i = 0; i < limit; i++) {
                Element item = items.get(i);
                SourceBookVO vo = new SourceBookVO();
                vo.setSourceId(source.getId());
                vo.setSourceName(source.getName());
                vo.setTitle(extractRule(item, firstText(rule, "ruleSearchName", "name", "bookName")));
                if (vo.getTitle() == null) vo.setTitle(extractRule(item, nestedText(rule, "ruleSearch", "name", "bookName")));
                if (vo.getTitle() == null) vo.setTitle(item.text());
                vo.setAuthor(extractRule(item, firstText(rule, "ruleSearchAuthor", "author", "bookAuthor")));
                if (vo.getAuthor() == null) vo.setAuthor(extractRule(item, nestedText(rule, "ruleSearch", "author", "bookAuthor")));
                vo.setDescription(extractRule(item, firstText(rule, "ruleSearchIntroduce", "ruleSearchDesc", "intro", "desc")));
                if (vo.getDescription() == null) vo.setDescription(extractRule(item, nestedText(rule, "ruleSearch", "intro", "desc")));
                vo.setCoverUrl(resolveUrl(source, extractRule(item, firstText(rule, "ruleSearchCoverUrl", "coverUrl", "cover"))));
                if (vo.getCoverUrl() == null) {
                    vo.setCoverUrl(resolveUrl(source, extractRule(item, nestedText(rule, "ruleSearch", "coverUrl", "cover"))));
                }
                if (vo.getCoverUrl() == null) {
                    vo.setCoverUrl(resolveUrl(source, findImageUrl(item)));
                }
                String detailUrl = extractRule(item, firstText(rule, "ruleSearchNoteUrl", "ruleSearchBookUrl", "bookUrl", "detailUrl"));
                if (detailUrl == null || detailUrl.isBlank()) {
                    detailUrl = extractRule(item, nestedText(rule, "ruleSearch", "bookUrl", "detailUrl", "url"));
                }
                if (detailUrl == null || detailUrl.isBlank()) detailUrl = item.hasAttr("href") ? item.absUrl("href") : null;
                vo.setContentUrl(resolveUrl(source, detailUrl));
                sanitizeBookResult(vo, keyword);
                if (vo.getTitle() != null && vo.getContentUrl() != null && matchesKeyword(vo, keyword)) result.add(vo);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("规则型书源配置无法解析");
        }
    }

    private boolean matchesKeyword(SourceBookVO book, String keyword) {
        if (book == null || keyword == null || keyword.isBlank()) return true;
        String normalizedKeyword = keyword.replaceAll("\\s+", "").toLowerCase();
        String text = ((book.getTitle() == null ? "" : book.getTitle()) + " "
                + (book.getAuthor() == null ? "" : book.getAuthor()) + " "
                + (book.getDescription() == null ? "" : book.getDescription())).replaceAll("\\s+", "").toLowerCase();
        if (!text.contains(normalizedKeyword)) return false;
        return !containsAny(text, "高h", "ntr", "np", "sm", "调教", "淫", "情色", "成人", "肉文", "禁忌", "屌", "绿萧");
    }

    private void sanitizeBookResult(SourceBookVO book, String keyword) {
        if (book == null) return;
        book.setTitle(cleanTextField(book.getTitle(), 120));
        book.setAuthor(cleanAuthorField(book.getAuthor(), book.getTitle(), keyword));
        book.setDescription(cleanTextField(book.getDescription(), 260));
        book.setCoverUrl(cleanTextField(book.getCoverUrl(), 1000));
        book.setContentUrl(cleanTextField(book.getContentUrl(), 1000));
    }

    private String cleanAuthorField(String author, String title, String keyword) {
        String cleaned = cleanTextField(author, 80);
        if (cleaned == null || cleaned.isBlank()) return null;
        String titleText = title == null ? "" : title;
        String keywordText = keyword == null ? "" : keyword;
        if (cleaned.length() > 36 && (cleaned.contains(titleText) || cleaned.contains(keywordText)
                || cleaned.matches(".*(简介|推荐度|连载|完结|章节|万字|第\\s*\\d+).*"))) {
            return null;
        }
        return cleaned;
    }

    private String cleanTextField(String value, int maxLength) {
        if (value == null) return null;
        String cleaned = value.replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) return null;
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }

    private String findImageUrl(Element item) {
        Element image = item == null ? null : item.selectFirst("img");
        if (image == null) return null;
        for (String attr : List.of("data-src", "data-original", "data-lazy-src", "src")) {
            String value = image.hasAttr(attr) ? image.attr(attr) : null;
            if (value != null && !value.isBlank()) return value.trim();
        }
        return null;
    }

    private boolean isSearchCandidate(BookSource source) {
        if (source == null || source.getEnabled() == null || source.getEnabled() == 0) return false;
        String ruleConfig = source.getRuleConfig();
        String searchUrl = source.getSearchUrl();
        JsonNode rule = null;
        if (ruleConfig != null && !ruleConfig.isBlank()) {
            try {
                rule = objectMapper.readTree(ruleConfig);
                if (searchUrl == null || searchUrl.isBlank()) {
                    searchUrl = firstText(rule, "ruleSearchUrl", "searchUrl");
                    if (searchUrl == null || searchUrl.isBlank()) searchUrl = nestedText(rule, "ruleSearch", "searchUrl", "url");
                }
                JsonNode type = rule.get("bookSourceType");
                if (type != null && type.isNumber() && type.asInt() == 2) return false;
                String chapterList = nestedText(rule, "ruleToc", "chapterList", "list");
                String contentRule = nestedText(rule, "ruleContent", "content", "body");
                if (isUnsupportedRule(chapterList) || isUnsupportedRule(contentRule)) return false;
            } catch (IOException ignored) {
                return false;
            }
        }
        if (searchUrl == null || searchUrl.isBlank() || isUnsupportedSearchUrl(searchUrl)) return false;
        String text = ((source.getName() == null ? "" : source.getName()) + " "
                + (source.getDescription() == null ? "" : source.getDescription()) + " "
                + (source.getBaseUrl() == null ? "" : source.getBaseUrl())).toLowerCase();
        return !containsAny(text, "漫画", "动漫", "视频", "短剧", "成人", "韩漫", "绅士", "gay", "腐漫", "搬山人",
                "bbqqgg", "66story", "wnacg", "禁下载", "人机验证", "网页登录", "卡密", "购买");
    }

    private int searchPriority(BookSource source) {
        String text = ((source.getName() == null ? "" : source.getName()) + " "
                + (source.getDescription() == null ? "" : source.getDescription()) + " "
                + (source.getBaseUrl() == null ? "" : source.getBaseUrl())).toLowerCase();
        int score = 0;
        if (containsAny(text, "小说", "书库", "书吧", "书阁", "阅读", "文学", "看书", "笔趣", "69书", "番茄")) score += 20;
        if (containsAny(text, "精品", "网络文学", "轻小说", "奇书", "起点")) score += 8;
        if (source.getSearchUrl() != null && source.getSearchUrl().startsWith("http")) score += 4;
        if (containsAny(text, "验证码", "登录", "限免", "反爬", "接口", "api")) score -= 6;
        return score;
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null || text.isBlank()) return false;
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) return true;
        }
        return false;
    }

    private boolean isUnsupportedSearchUrl(String searchUrl) {
        if (searchUrl == null || searchUrl.isBlank()) return true;
        String value = searchUrl.trim();
        return value.startsWith("@js")
                || value.startsWith("js:")
                || value.startsWith("<js>")
                || value.contains("java.")
                || value.contains("java.startBrowser")
                || value.contains("\"webView\"")
                || value.contains("{\"method\":\"POST\"")
                || value.contains(",{");
    }

    private boolean isUnsupportedRule(String rule) {
        if (rule == null || rule.isBlank()) return false;
        String value = rule.trim();
        return value.startsWith("@js")
                || value.startsWith("js:")
                || value.startsWith("<js>")
                || value.contains("java.")
                || value.contains("function(");
    }

    private String cleanLegadoUrl(String url) {
        if (url == null) return "";
        return url.replaceAll("<,/?\\{\\{page}}>", "")
                .replaceAll("<,/?\\{page}>", "")
                .trim();
    }

    private String normalizeSubscribeUrl(String url) {
        URI uri = validateHttpUrl(url);
        String host = uri.getHost();
        if (host != null && host.equalsIgnoreCase("shuyuan.yiove.com")) {
            return "https://shuyuan-api.yiove.com/import/book-sources/1-100";
        }
        return url;
    }

    private String fetchSourceContent(BookSource source, URI uri) {
        if (!"rule_source".equals(source.getSourceType())
                || source.getRuleConfig() == null
                || source.getRuleConfig().isBlank()) {
            return fetchText(uri);
        }
        try {
            JsonNode rule = objectMapper.readTree(source.getRuleConfig());
            String detailBody = fetchText(uri);
            if (!detailBody.trim().startsWith("<")) return detailBody;

            Document detailDocument = Jsoup.parse(detailBody, uri.toString());
            String tocUrl = extractRule(detailDocument, nestedText(rule, "ruleBookInfo", "tocUrl"));
            URI tocUri = uri;
            Document tocDocument = detailDocument;
            if (tocUrl != null && !tocUrl.isBlank()) {
                tocUri = validateHttpUrl(resolveUrl(source, tocUrl));
                String tocBody = fetchText(tocUri);
                if (tocBody.trim().startsWith("<")) {
                    tocDocument = Jsoup.parse(tocBody, tocUri.toString());
                }
            }

            List<Element> chapterItems = collectChapterItems(source, rule, tocDocument, tocUri);

            StringBuilder bookContent = new StringBuilder();
            int limit = Math.min(chapterItems.size(), 240);
            for (int i = 0; i < limit; i++) {
                Element item = chapterItems.get(i);
                String chapterName = extractRule(item, nestedText(rule, "ruleToc", "chapterName", "name", "title"));
                if (chapterName == null || chapterName.isBlank()) chapterName = item.text();
                String chapterUrl = extractRule(item, nestedText(rule, "ruleToc", "chapterUrl", "url", "href"));
                if (chapterUrl == null || chapterUrl.isBlank()) chapterUrl = item.hasAttr("href") ? item.absUrl("href") : null;
                chapterUrl = resolveUrl(source, chapterUrl);
                if (chapterUrl == null || chapterUrl.isBlank()) continue;
                try {
                    String chapterBody = fetchText(validateHttpUrl(chapterUrl));
                    String chapterContent = extractChapterContent(rule, chapterBody, chapterUrl);
                    if (chapterContent == null || chapterContent.isBlank()) continue;
                    bookContent.append(formatChapterTitle(i, chapterName))
                            .append('\n')
                            .append(cleanChapterText(chapterContent))
                            .append("\n\n");
                } catch (RuntimeException ignored) {
                    // 单章失败时继续尝试下一章
                }
            }

            if (!bookContent.isEmpty()) return bookContent.toString();

            String directContent = extractChapterContent(rule, detailBody, uri.toString());
            if (directContent == null || directContent.isBlank()) {
                throw new RuntimeException("规则型书源未解析到正文");
            }
            return formatChapterTitle(0, "正文") + "\n" + cleanChapterText(directContent);
        } catch (IOException e) {
            throw new RuntimeException("规则型书源配置无法解析");
        }
    }

    private List<Element> collectChapterItems(BookSource source, JsonNode rule, Document firstDocument, URI firstUri) {
        List<Element> allItems = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Document currentDocument = firstDocument;
        URI currentUri = firstUri;
        String chapterListRule = nestedText(rule, "ruleToc", "chapterList", "list");
        String nextRule = nestedText(rule, "ruleToc", "nextTocUrl", "nextUrl");

        for (int page = 0; page < 12 && currentDocument != null && allItems.size() < 300; page++) {
            visited.add(currentUri.toString());
            Elements pageItems = selectElements(currentDocument, chapterListRule);
            if (pageItems.isEmpty()) {
                pageItems = currentDocument.select(".chapter-list a, .catalog a, .chapter a, #list a, .listmain a, a[href]");
            }
            allItems.addAll(pageItems);

            String nextUrl = extractRule(currentDocument, nextRule);
            nextUrl = resolveUrl(source, nextUrl);
            if (nextUrl == null || nextUrl.isBlank() || visited.contains(nextUrl)) break;
            try {
                currentUri = validateHttpUrl(nextUrl);
                String body = fetchText(currentUri);
                if (!body.trim().startsWith("<")) break;
                currentDocument = Jsoup.parse(body, currentUri.toString());
            } catch (RuntimeException ignored) {
                break;
            }
        }
        return allItems;
    }

    private String extractChapterContent(JsonNode rule, String body, String url) {
        if (body == null || body.isBlank()) return null;
        if (!body.trim().startsWith("<")) return body;
        Document document = Jsoup.parse(body, url);
        String contentRule = firstText(rule, "ruleContentContent", "ruleBookContent", "contentRule", "content");
        if (contentRule == null) contentRule = nestedText(rule, "ruleContent", "content", "body");
        String content = extractRule(document, contentRule);
        if (content == null || content.isBlank()) {
            content = document.select("article, .content, #content, .chapter, .chapter-content, .read-content, .chapter_content_box").text();
        }
        if (content == null || content.isBlank()) return null;
        return cleanChapterText(content);
    }

    private String formatChapterTitle(int index, String rawTitle) {
        String title = cleanChapterText(rawTitle);
        if (title == null || title.isBlank()) return "第" + (index + 1) + "章";
        title = title.replaceAll("^\\s*第\\s*[0-9零〇一二两三四五六七八九十百千万]+\\s*[章章节回卷部集]\\s*", "").trim();
        if (title.length() > 36) title = title.substring(0, 36);
        return "第" + (index + 1) + "章" + (title.isBlank() ? "" : " " + title);
    }

    private String cleanChapterText(String value) {
        if (value == null) return null;
        String text = value
                .replace("&nbsp;", " ")
                .replaceAll("(?i)<\\s*br\\s*/?>", "\n")
                .replaceAll("(?i)</\\s*p\\s*>", "\n")
                .replaceAll("(?i)</\\s*div\\s*>", "\n")
                .replaceAll("(?i)</\\s*li\\s*>", "\n");
        if (text.contains("<") && text.contains(">")) {
            text = Jsoup.parse(text).text();
        }
        return text
                .replace('\u00A0', ' ')
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("(?<=[。！？!?])\\s+", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private BookSource getUserSource(Integer userId, Integer sourceId) {
        BookSource source = getOne(new LambdaQueryWrapper<BookSource>()
                .eq(BookSource::getId, sourceId)
                .eq(BookSource::getUserId, userId));
        if (source == null) throw new RuntimeException("书源不存在");
        return source;
    }

    private JsonNode findResultList(JsonNode root) {
        if (root == null) return null;
        if (root.isArray()) return root;
        for (String key : List.of("data", "books", "items", "list", "results", "sources", "bookSources")) {
            JsonNode node = root.get(key);
            if (node == null) continue;
            if (node.isArray()) return node;
            JsonNode nested = findResultList(node);
            if (nested != null) return nested;
        }
        return null;
    }

    private BookSourceDTO normalizeSource(JsonNode item, String subscribeUrl, int index) {
        String name = text(item, "name", "sourceName", "bookSourceName");
        String baseUrl = text(item, "baseUrl", "bookSourceUrl", "sourceUrl", "url");
        String searchUrl = text(item, "searchUrl", "ruleSearchUrl", "search");
        String contentUrlTemplate = text(item, "contentUrlTemplate", "contentUrl", "ruleContentUrl");
        if ((name == null || name.isBlank()) && (baseUrl == null || baseUrl.isBlank())) return null;

        BookSourceDTO dto = new BookSourceDTO();
        dto.setName(name != null ? name : "订阅书源 " + (index + 1));
        dto.setSourceKey("sub-" + Math.abs((subscribeUrl + "-" + index + "-" + dto.getName()).hashCode()));
        boolean legadoRule = item.has("bookSourceName")
                || item.has("bookSourceUrl")
                || item.has("ruleSearchList")
                || item.has("ruleBookInfo")
                || item.has("ruleContent");
        dto.setSourceType(legadoRule ? "rule_source" : (searchUrl != null && !searchUrl.isBlank() ? "json_api" : "rule_source"));
        dto.setBaseUrl(baseUrl != null ? baseUrl : "");
        dto.setSearchUrl(shortField(searchUrl));
        dto.setContentUrlTemplate(shortField(contentUrlTemplate));
        dto.setRuleConfig(item.toString());
        dto.setDescription(text(item, "description", "desc", "sourceComment", "bookSourceComment", "comment"));
        dto.setLanguage(text(item, "language", "lang"));
        if (dto.getLanguage() == null) dto.setLanguage("custom");
        String group = text(item, "sourceGroup", "bookSourceGroup", "group");
        if (group != null && !group.isBlank() && (dto.getDescription() == null || dto.getDescription().isBlank())) {
            dto.setDescription("分组: " + group);
        }
        JsonNode enabled = item.get("enabled");
        dto.setEnabled(enabled != null && enabled.isBoolean() ? (enabled.asBoolean() ? 1 : 0) : 1);
        dto.setIsPreset(0);
        return dto;
    }

    private String shortField(String value) {
        if (value == null || value.isBlank()) return "";
        return value.length() <= 760 ? value : "";
    }

    private String extractSubscriptionUrl(String html) {
        Matcher matcher = Pattern.compile("https?://[^\\s\"'<>]+", Pattern.CASE_INSENSITIVE).matcher(html);
        while (matcher.find()) {
            String found = matcher.group();
            if (found.contains("bitbucket.org/xiu2/yuedu/raw")
                    || found.contains("raw.githubusercontent.com")
                    || found.contains("cdn.jsdmirror.com/gh/XIU2/Yuedu/shuyuan")
                    || found.contains("jsd.onmicrosoft.cn/gh/XIU2/Yuedu/shuyuan")) {
                return found;
            }
        }
        return null;
    }

    private BookSourceDTO sourceFromWebEntry(String url, String html) {
        BookSourceDTO dto = new BookSourceDTO();
        String host = validateHttpUrl(url).getHost();
        String title = extractTitle(html);
        dto.setName(title != null ? title : host);
        dto.setSourceKey("web-" + Math.abs(url.hashCode()));
        dto.setSourceType("rule_source");
        dto.setBaseUrl(url);
        dto.setSearchUrl("");
        dto.setContentUrlTemplate("");
        dto.setRuleConfig("");
        dto.setDescription("网页/规则型书源入口，已保存；搜索下载需要后续规则引擎支持。");
        dto.setLanguage("custom");
        dto.setEnabled(1);
        dto.setIsPreset(0);
        return dto;
    }

    private String extractTitle(String html) {
        Matcher matcher = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(html);
        if (matcher.find()) {
            return matcher.group(1).replaceAll("\\s+", " ").trim();
        }
        return null;
    }

    private String text(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && !value.isNull()) {
                String text = value.asText();
                if (text != null && !text.isBlank()) return text.trim();
            }
        }
        return null;
    }

    private String resolveUrl(BookSource source, String url) {
        if (url == null || url.isBlank()) return null;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        String base = source.getBaseUrl();
        if (base == null || base.isBlank()) return url;
        if (base.endsWith("/") && url.startsWith("/")) return base.substring(0, base.length() - 1) + url;
        if (!base.endsWith("/") && !url.startsWith("/")) return base + "/" + url;
        return base + url;
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
            Optional<String> contentType = response.headers().firstValue("Content-Type");
            return decodeText(bytes, contentType.orElse(null));
        } catch (IOException e) {
            throw new RuntimeException("无法连接书源: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("书源请求被中断");
        }
    }

    private String decodeText(byte[] bytes) {
        return decodeText(bytes, null);
    }

    private String decodeText(byte[] bytes, String contentType) {
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xef
                && (bytes[1] & 0xff) == 0xbb
                && (bytes[2] & 0xff) == 0xbf) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        String charset = extractCharset(contentType);
        if (charset == null) {
            String asciiHead = new String(bytes, 0, Math.min(bytes.length, 4096), StandardCharsets.ISO_8859_1);
            charset = extractCharset(asciiHead);
        }
        if (charset != null) {
            try {
                return new String(bytes, Charset.forName(charset));
            } catch (Exception ignored) {
                // 继续使用兜底探测
            }
        }
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (utf8.indexOf('\uFFFD') >= 0) {
            return new String(bytes, Charset.forName("GB18030"));
        }
        return utf8;
    }

    private String extractCharset(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher matcher = Pattern.compile("charset\\s*=\\s*['\"]?([a-zA-Z0-9_\\-]+)", Pattern.CASE_INSENSITIVE).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
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
        dto.setSearchUrl(source.getSearchUrl());
        dto.setContentUrlTemplate(source.getContentUrlTemplate());
        dto.setRuleConfig(source.getRuleConfig());
        dto.setDescription(source.getDescription());
        dto.setLanguage(source.getLanguage());
        dto.setEnabled(source.getEnabled());
        dto.setIsPreset(source.getIsPreset());
        return dto;
    }

    private String firstText(JsonNode node, String... keys) {
        String value = text(node, keys);
        return value == null || value.isBlank() ? null : value;
    }

    private String nestedText(JsonNode node, String objectKey, String... keys) {
        JsonNode nested = node != null ? node.get(objectKey) : null;
        if (nested == null) return null;
        return text(nested, keys);
    }

    private Elements selectElements(Element root, String rule) {
        Elements current = new Elements(root);
        if (rule == null || rule.isBlank()) return current;
        if (isUnsupportedRule(rule)) return new Elements();
        for (String part : rule.split("@")) {
            String token = part.trim();
            if (token.isEmpty() || isAttrToken(token)) continue;
            Elements next = new Elements();
            String css = toCssSelector(token);
            Integer index = selectorIndex(token);
            for (Element element : current) {
                Elements selected = element.select(css);
                if (index != null) {
                    if (index >= 0 && index < selected.size()) next.add(selected.get(index));
                } else {
                    next.addAll(selected);
                }
            }
            if (!next.isEmpty()) current = next;
        }
        return current;
    }

    private String extractRule(Element root, String rule) {
        if (rule == null || rule.isBlank()) return null;
        if (isUnsupportedRule(rule)) return null;
        String[] parts = rule.split("@");
        String attr = "text";
        StringBuilder selector = new StringBuilder();
        for (String raw : parts) {
            String token = raw.trim();
            if (token.isEmpty()) continue;
            if (isAttrToken(token)) {
                attr = token;
            } else {
                if (!selector.isEmpty()) selector.append("@");
                selector.append(token);
            }
        }
        Elements elements = selectElements(root, selector.toString());
        Element target = elements.isEmpty() ? root : elements.first();
        return extractAttr(target, attr);
    }

    private String extractAttr(Element element, String attr) {
        if (element == null) return null;
        String value;
        switch (attr) {
            case "text":
            case "all":
                value = element.text();
                break;
            case "ownText":
                value = element.ownText();
                break;
            case "html":
                value = element.html();
                break;
            case "href":
                value = element.absUrl("href");
                break;
            case "src":
                value = element.absUrl("src");
                break;
            default:
                String attrName = attr.startsWith("attr.") ? attr.substring(5) : attr;
                value = element.hasAttr(attrName) ? element.attr(attrName) : element.text();
                break;
        }
        return value != null && !value.isBlank() ? value.trim() : null;
    }

    private boolean isAttrToken(String token) {
        return List.of("text", "ownText", "html", "href", "src", "all").contains(token)
                || token.startsWith("attr.");
    }

    private String toCssSelector(String token) {
        token = stripSelectorIndex(token);
        if (token.startsWith("class.")) return "." + token.substring(6);
        if (token.startsWith("id.")) return "#" + token.substring(3);
        if (token.startsWith("tag.")) return token.substring(4);
        if (token.startsWith("attr.")) {
            String attr = token.substring(5);
            int dot = attr.indexOf('.');
            if (dot > 0) return "[" + attr.substring(0, dot) + "=" + attr.substring(dot + 1) + "]";
            return "[" + attr + "]";
        }
        return token;
    }

    private Integer selectorIndex(String token) {
        if (token == null || token.startsWith("attr.")) return null;
        Matcher matcher = Pattern.compile("^(.*)\\.(\\d+)$").matcher(token);
        if (!matcher.matches()) return null;
        return Integer.parseInt(matcher.group(2));
    }

    private String stripSelectorIndex(String token) {
        if (token == null || token.startsWith("attr.")) return token;
        Matcher matcher = Pattern.compile("^(.*)\\.\\d+$").matcher(token);
        return matcher.matches() ? matcher.group(1) : token;
    }

    private static BookSourceDTO preset(String key, String name, String type, String baseUrl,
                                        String searchUrl, String contentUrlTemplate,
                                        String description, String language) {
        BookSourceDTO dto = new BookSourceDTO();
        dto.setSourceKey(key);
        dto.setName(name);
        dto.setSourceType(type);
        dto.setBaseUrl(baseUrl);
        dto.setSearchUrl(searchUrl);
        dto.setContentUrlTemplate(contentUrlTemplate);
        dto.setDescription(description);
        dto.setLanguage(language);
        dto.setEnabled(0);
        dto.setIsPreset(1);
        return dto;
    }
}

package com.moeread.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Legado 阅读 APP 默认 TXT 章节识别规则 (移植版)
 *
 * 来源仓库: gedoor/legado (原仓库已清空, 通过活跃 fork Luoyacheng/legado 获取, 分支 main)
 * 原始文件: app/src/main/assets/defaultData/txtTocRule.json
 * 加载逻辑: io.legado.app.help.DefaultData#txtTocRules
 * 实体类:   io.legado.app.data.entities.TxtTocRule  (字段: id, name, rule, replacement, example, serialNumber, enable)
 * 核心算法: io.legado.app.model.localBook.TextFile (analyze / getTocRule)
 *
 * 字段说明:
 *   name        规则名称
 *   rule        正则表达式 (Java Pattern, 需用 Pattern.MULTILINE 编译)
 *   replacement 标题净化 JS 脚本 (此处为空, Java 移植版不执行 JS, 直接用 matcher.group())
 *   enable      是否默认启用
 *   serialNumber 排序序号
 *
 * 共 26 条规则 (id -1 ~ -25, 以及 -100 兜底空规则)
 * 默认 enable=true 的有 12 条: -1, -2, -8, -9, -11, -12, -14, -16, -17, -21, -22, -24
 */
public final class LegadoTxtTocRules {

    private LegadoTxtTocRules() {}

    /** 规则定义: {name, rule, enable, serialNumber} */
    public static final String[][] RULES = new String[][]{
        // 默认启用的 12 条 (按 serialNumber 升序, 即 Legado 默认评估顺序)
        {"目录(去空白)",
         "(?<=[　\\s])(?:序章|楔子|正文(?!完|结)|终章|后记|尾声|番外|第\\s{0,4}[\\d〇零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]+?\\s{0,4}(?:章|节(?!课)|卷|集(?![合和]))).{0,30}$",
         "true", "0"},

        {"目录",
         "^[ 　\\t]{0,4}(?:序章|楔子|正文(?!完|结)|终章|后记|尾声|番外|第\\s{0,4}[\\d〇零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]+?\\s{0,4}(?:章|节(?!课)|卷|集(?![合和])|部(?![分赛游])|篇(?!张))).{0,30}$",
         "true", "1"},

        {"数字 分隔符 标题名称",
         "^[ 　\\t]{0,4}\\d{1,5}[:：,.， 、_—\\-].{1,30}$",
         "true", "7"},

        {"大写数字 分隔符 标题名称",
         "^[ 　\\t]{0,4}(?:序章|楔子|正文(?!完|结)|终章|后记|尾声|番外|[零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,8}章?)[ 、_—\\-].{1,30}$",
         "true", "8"},

        {"正文 标题/序号",
         "^[ 　\\t]{0,4}正文[ 　]{1,4}.{0,20}$",
         "true", "10"},

        {"Chapter/Section/Part/Episode 序号 标题",
         "^[ 　\\t]{0,4}(?:[Cc]hapter|[Ss]ection|[Pp]art|ＰＡＲＴ|[Nn][oO][.、]|[Ee]pisode|(?:内容|文章)?简介|文案|前言|序章|楔子|正文(?!完|结)|终章|后记|尾声|番外)\\s{0,4}\\d{1,4}.{0,30}$",
         "true", "11"},

        {"特殊符号 序号 标题",
         "(?<=[\\s　])[【〔〖「『〈［\\[](?:第|[Cc]hapter)[\\d零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10}[章节].{0,20}$",
         "true", "13"},

        {"特殊符号 标题(单个)",
         "(?<=[\\s　]{0,4})(?:[☆★✦✧].{1,30}|(?:内容|文章)?简介|文案|前言|序章|楔子|正文(?!完|结)|终章|后记|尾声|番外)[ 　]{0,4}$",
         "true", "15"},

        {"章/卷 序号 标题",
         "^[ \\t　]{0,4}(?:(?:内容|文章)?简介|文案|前言|序章|楔子|正文(?!完|结)|终章|后记|尾声|番外|[卷章][\\d零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,8})[ 　]{0,4}.{0,30}$",
         "true", "16"},

        {"书名 括号 序号",
         "^[一-龥]{1,20}[ 　\\t]{0,4}[(（][\\d〇零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,8}[)）][ 　\\t]{0,4}$",
         "true", "20"},

        {"书名 序号",
         "^[一-龥]{1,20}[ 　\\t]{0,4}[\\d〇零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,8}[ 　\\t]{0,4}$",
         "true", "21"},

        {"字数分割 分节阅读",
         "(?<=[ 　\\t]{0,4})(?:.{0,15}分[页节章段]阅读[-_ ]|第\\s{0,4}[\\d零一二两三四五六七八九十百千万]{1,6}\\s{0,4}[页节]).{0,30}$",
         "true", "23"},

        // 默认禁用的 13 条 (备用规则, 可按需启用)
        {"目录(匹配简介)",
         "(?<=[　\\s])(?:(?:内容|文章)?简介|文案|前言|序章|楔子|正文(?!完|结)|终章|后记|尾声|番外|第\\s{0,4}[\\d〇零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]+?\\s{0,4}(?:章|节(?!课)|卷|集(?![合和])|部(?![分赛游])|回(?![合来事去])|场(?![和合比电是])|篇(?!张))).{0,30}$",
         "false", "2"},

        {"目录(古典、轻小说备用)",
         "^[ 　\\t]{0,4}(?:序章|楔子|正文(?!完|结)|终章|后记|尾声|番外|第\\s{0,4}[\\d〇零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]+?\\s{0,4}(?:章|节(?!课)|卷|集(?![合和])|部(?![分赛游])|回(?![合来事去])|场(?![和合比电是])|话|篇(?!张))).{0,30}$",
         "false", "3"},

        {"数字(纯数字标题)",
         "(?<=[　\\s])\\d+\\.?[ 　\\t]{0,4}$",
         "false", "4"},

        {"大写数字(纯数字标题)",
         "(?<=[　\\s])[零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,12}[ 　\\t]{0,4}$",
         "false", "5"},

        {"数字混合(纯数字标题)",
         "(?<=[　\\s])[零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟\\d]{1,12}[ 　\\t]{0,4}$",
         "false", "6"},

        {"数字混合 分隔符 标题名称",
         "^[ 　\\t]{0,4}(?:序章|楔子|正文(?!完|结)|终章|后记|尾声|番外|[零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,8}章?[ 、_—\\-]|\\d{1,5}章?[:：,.， 、_—\\-]).{0,30}$",
         "false", "9"},

        {"Chapter(去简介)",
         "^[ 　\\t]{0,4}(?:[Cc]hapter|[Ss]ection|[Pp]art|ＰＡＲＴ|[Nn][Oo]\\.|[Ee]pisode)\\s{0,4}\\d{1,4}.{0,30}$",
         "false", "12"},

        {"特殊符号 标题(成对)",
         "(?<=[\\s　]{0,4})(?:[\\[〈「『〖〔《（【\\(].{1,30}[\\)】）》〕〗』」〉\\]]?|(?:内容|文章)?简介|文案|前言|序章|楔子|正文(?!完|结)|终章|后记|尾声|番外)[ 　]{0,4}$",
         "false", "14"},

        {"顶格标题",
         "^\\S.{1,20}$",
         "false", "17"},

        {"双标题(前向)",
         "(?m)(?<=[ \\t　]{0,4})第[\\d〇零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,8}章.{0,30}$(?=[\\s　]{0,8}第[\\d零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,8}章)",
         "false", "18"},

        {"双标题(后向)",
         "(?m)(?<=[ \\t　]{0,4}第[\\d〇零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,8}章.{0,30}$[\\s　]{0,8})第[\\d零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,8}章.{0,30}$",
         "false", "19"},

        {"特定字符 标题 特定符号",
         "(?<=\\={3,6}).{1,40}?(?=\\=)",
         "false", "22"},

        {"通用规则",
         "(?im)^.{0,6}(?:[引楔]子|正文(?!完|结)|[引序前]言|[序终]章|扉页|[上中下][部篇卷]|卷首语|后记|尾声|番外|={2,4}|第\\s{0,4}[\\d〇零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]+?\\s{0,4}(?:章|节(?!课)|卷|页[、 　]|集(?![合和])|部(?![分是门落])|篇(?!张))).{0,40}$|^.{0,6}[\\d〇零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟a-z]{1,8}[、. 　].{0,20}$",
         "false", "24"},

        // id=-100 兜底规则 (rule 为空, 表示无规则, 按字数拆分), 此处省略
    };

    /**
     * Legado 选择最佳规则的核心算法 (移植自 TextFile#getTocRule):
     * 1. 取文件前 512KB 内容作为样本;
     * 2. 依次用每条启用的规则在样本上做 MULTILINE 匹配;
     * 3. 统计有效章节数 csNum (相邻匹配间距>1000字或首个, 且标题非空才算)
     *    和误判数 numE (相邻匹配间距<100字, 视为卷标题等误判);
     * 4. 要求 csNum >= numE * 3, 且 csNum 超过当前最大值至少 overRuleCount(=2) 才采纳;
     * 5. 一旦某规则 csNum > 70 立即选定, 不再评估后续规则.
     */
    public static int selectBestRule(String sample) {
        int maxNum = -1;
        int bestIndex = -1;
        final int overRuleCount = 2;
        for (int i = 0; i < RULES.length; i++) {
            if (!"true".equals(RULES[i][2])) continue;
            Pattern pattern;
            try {
                pattern = Pattern.compile(RULES[i][1], Pattern.MULTILINE);
            } catch (PatternSyntaxException e) {
                continue;
            }
            Matcher matcher = pattern.matcher(sample);
            int start = 0;
            int csNum = 0;
            int numE = 0;
            while (matcher.find()) {
                int contentLength = matcher.start() - start;
                if (start == 0 || contentLength > 1000) {
                    csNum++;
                } else if (contentLength < 100) {
                    numE++;
                }
                start = matcher.end();
            }
            if (csNum >= numE * 3 && csNum > maxNum + overRuleCount) {
                maxNum = csNum;
                bestIndex = i;
                if (maxNum > 70) break;
            }
        }
        return bestIndex;
    }

    /** 用指定规则切分全文, 返回每个章节标题在全文中的起始偏移列表 [start, end] */
    public static List<int[]> splitChapters(String fullText, int ruleIndex) {
        List<int[]> result = new ArrayList<>();
        if (ruleIndex < 0 || ruleIndex >= RULES.length) return result;
        Pattern pattern = Pattern.compile(RULES[ruleIndex][1], Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fullText);
        while (matcher.find()) {
            result.add(new int[]{matcher.start(), matcher.end()});
        }
        return result;
    }
}

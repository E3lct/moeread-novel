package com.moeread.controller;

import com.moeread.common.Result;
import com.moeread.config.LoginUser;
import com.moeread.dto.ChapterVO;
import com.moeread.service.ChapterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chapter")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    /** 章节目录 */
    @LoginUser
    @GetMapping("/toc/{bookId}")
    public Result<List<ChapterVO>> toc(@PathVariable Integer bookId) {
        return Result.ok(chapterService.getToc(bookId));
    }

    /** 章节内容 */
    @LoginUser
    @GetMapping("/{chapterId}")
    public Result<ChapterVO> content(@PathVariable Integer chapterId) {
        return Result.ok(chapterService.getContent(chapterId));
    }
}

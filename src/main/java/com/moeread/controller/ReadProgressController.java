package com.moeread.controller;

import com.moeread.common.Result;
import com.moeread.config.LoginUser;
import com.moeread.config.RequestContext;
import com.moeread.dto.SaveProgressDTO;
import com.moeread.entity.ReadProgress;
import com.moeread.service.ReadProgressService;
import com.moeread.service.ReadingStatsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/progress")
public class ReadProgressController {

    private final ReadProgressService progressService;
    private final ReadingStatsService statsService;

    public ReadProgressController(ReadProgressService progressService,
                                   ReadingStatsService statsService) {
        this.progressService = progressService;
        this.statsService = statsService;
    }

    /** 保存阅读进度，并累加阅读分钟 */
    @LoginUser
    @PostMapping("/save")
    public Result<?> save(@RequestBody SaveProgressDTO dto) {
        Integer userId = RequestContext.getUserId();
        progressService.saveProgress(userId, dto.getBookId(), dto.getChapterIndex(),
                dto.getScrollPosition());
        statsService.addTodayMinutes(userId, 1);
        return Result.ok();
    }

    /** 获取某书阅读进度 */
    @LoginUser
    @GetMapping("/{bookId}")
    public Result<ReadProgress> get(@PathVariable Integer bookId) {
        Integer userId = RequestContext.getUserId();
        return Result.ok(progressService.getProgress(userId, bookId));
    }
}

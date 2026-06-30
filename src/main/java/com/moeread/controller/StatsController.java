package com.moeread.controller;

import com.moeread.common.Result;
import com.moeread.config.LoginUser;
import com.moeread.config.RequestContext;
import com.moeread.dto.HeatmapPoint;
import com.moeread.dto.StatsOverviewVO;
import com.moeread.service.ReadingStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final ReadingStatsService statsService;

    public StatsController(ReadingStatsService statsService) {
        this.statsService = statsService;
    }

    /** 年度热力图 */
    @LoginUser
    @GetMapping("/heatmap")
    public Result<List<HeatmapPoint>> heatmap(@RequestParam(defaultValue = "2026") int year) {
        Integer userId = RequestContext.getUserId();
        return Result.ok(statsService.getYearlyHeatmap(userId, year));
    }

    /** 统计概览 */
    @LoginUser
    @GetMapping("/overview")
    public Result<StatsOverviewVO> overview() {
        Integer userId = RequestContext.getUserId();
        return Result.ok(statsService.getOverview(userId));
    }

    /** 阅读习惯（周分布） */
    @LoginUser
    @GetMapping("/habit")
    public Result<int[]> habit() {
        Integer userId = RequestContext.getUserId();
        return Result.ok(statsService.getWeeklyHabit(userId));
    }
}

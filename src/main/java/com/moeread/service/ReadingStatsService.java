package com.moeread.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moeread.entity.ReadingStats;
import com.moeread.dto.HeatmapPoint;
import com.moeread.dto.StatsOverviewVO;

import java.util.List;

public interface ReadingStatsService extends IService<ReadingStats> {

    /** 累加今日阅读分钟 */
    void addTodayMinutes(Integer userId, int minutes);

    /** 年度热力图数据 */
    List<HeatmapPoint> getYearlyHeatmap(Integer userId, int year);

    /** 统计概览 */
    StatsOverviewVO getOverview(Integer userId);

    /** 每日阅读习惯（一周分布） */
    int[] getWeeklyHabit(Integer userId);
}

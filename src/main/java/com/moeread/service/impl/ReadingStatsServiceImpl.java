package com.moeread.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moeread.dto.HeatmapPoint;
import com.moeread.dto.StatsOverviewVO;
import com.moeread.entity.Book;
import com.moeread.entity.ReadingStats;
import com.moeread.mapper.BookMapper;
import com.moeread.mapper.ReadingStatsMapper;
import com.moeread.service.ReadingStatsService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReadingStatsServiceImpl extends ServiceImpl<ReadingStatsMapper, ReadingStats>
        implements ReadingStatsService {

    private final BookMapper bookMapper;

    public ReadingStatsServiceImpl(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    @Override
    public void addTodayMinutes(Integer userId, int minutes) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        ReadingStats stats = getOne(new LambdaQueryWrapper<ReadingStats>()
                .eq(ReadingStats::getUserId, userId)
                .eq(ReadingStats::getDate, today));
        if (stats == null) {
            stats = new ReadingStats();
            stats.setUserId(userId);
            stats.setDate(today);
            stats.setMinutes(minutes);
        } else {
            stats.setMinutes(stats.getMinutes() + minutes);
        }
        saveOrUpdate(stats);
    }

    @Override
    public List<HeatmapPoint> getYearlyHeatmap(Integer userId, int year) {
        List<ReadingStats> stats = list(new LambdaQueryWrapper<ReadingStats>()
                .eq(ReadingStats::getUserId, userId)
                .ge(ReadingStats::getDate, year + "-01-01")
                .le(ReadingStats::getDate, year + "-12-31"));

        List<HeatmapPoint> points = new ArrayList<>();
        for (ReadingStats rs : stats) {
            HeatmapPoint p = new HeatmapPoint();
            p.setDate(rs.getDate());
            p.setMinutes(rs.getMinutes());
            points.add(p);
        }
        return points;
    }

    @Override
    public StatsOverviewVO getOverview(Integer userId) {
        StatsOverviewVO vo = new StatsOverviewVO();

        // 总阅读分钟
        List<ReadingStats> all = list(new LambdaQueryWrapper<ReadingStats>()
                .eq(ReadingStats::getUserId, userId));
        long totalMinutes = 0;
        for (ReadingStats rs : all) totalMinutes += rs.getMinutes();
        vo.setTotalMinutes(totalMinutes);

        // 总书籍数
        vo.setTotalBooks(bookMapper.selectCount(new LambdaQueryWrapper<Book>()
                .eq(Book::getUserId, userId)));

        // 连续打卡天数
        vo.setStreakDays(calcStreak(all));

        return vo;
    }

    @Override
    public int[] getWeeklyHabit(Integer userId) {
        // 统计本周每天阅读分钟
        List<ReadingStats> stats = list(new LambdaQueryWrapper<ReadingStats>()
                .eq(ReadingStats::getUserId, userId)
                .ge(ReadingStats::getDate, LocalDate.now().minusWeeks(12).toString()));

        int[] dailyMinutes = new int[7];
        for (ReadingStats rs : stats) {
            try {
                LocalDate d = LocalDate.parse(rs.getDate());
                int dow = d.getDayOfWeek().getValue() - 1; // Mon=0, Sun=6
                dailyMinutes[dow] += rs.getMinutes();
            } catch (Exception ignored) {}
        }
        return dailyMinutes;
    }

    private long calcStreak(List<ReadingStats> stats) {
        if (stats.isEmpty()) return 0;
        Set<String> activeDays = new TreeSet<>();
        for (ReadingStats rs : stats) {
            if (rs.getMinutes() > 0) activeDays.add(rs.getDate());
        }
        List<String> sorted = new ArrayList<>(activeDays);
        Collections.reverse(sorted);

        long streak = 0;
        LocalDate cursor = LocalDate.now();
        for (String dateStr : sorted) {
            LocalDate d = LocalDate.parse(dateStr);
            if (d.equals(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (d.isBefore(cursor)) {
                break;
            }
        }
        return streak;
    }
}

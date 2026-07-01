package com.moeread.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moeread.dto.HeatmapPoint;
import com.moeread.dto.StatsOverviewVO;
import com.moeread.entity.Book;
import com.moeread.entity.ReadingStats;
import com.moeread.mapper.BookMapper;
import com.moeread.mapper.ReadingStatsMapper;
import com.moeread.mapper.UserMapper;
import com.moeread.service.ReadingStatsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReadingStatsServiceImpl extends ServiceImpl<ReadingStatsMapper, ReadingStats>
        implements ReadingStatsService {

    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    public ReadingStatsServiceImpl(BookMapper bookMapper, UserMapper userMapper) {
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void addTodayMinutes(Integer userId, int minutes) {
        LocalDate today = LocalDate.now();
        ReadingStats stats = getOne(new LambdaQueryWrapper<ReadingStats>()
                .eq(ReadingStats::getUserId, userId)
                .eq(ReadingStats::getStatDate, today));
        if (stats == null) {
            stats = new ReadingStats();
            stats.setUserId(userId);
            stats.setStatDate(today);
            stats.setReadMinutes(minutes);
        } else {
            stats.setReadMinutes(stats.getReadMinutes() + minutes);
        }
        saveOrUpdate(stats);
    }

    @Override
    public List<HeatmapPoint> getYearlyHeatmap(Integer userId, int year) {
        List<ReadingStats> stats = list(new LambdaQueryWrapper<ReadingStats>()
                .eq(ReadingStats::getUserId, userId)
                .ge(ReadingStats::getStatDate, LocalDate.of(year, 1, 1))
                .le(ReadingStats::getStatDate, LocalDate.of(year, 12, 31)));

        List<HeatmapPoint> points = new ArrayList<>();
        for (ReadingStats rs : stats) {
            HeatmapPoint p = new HeatmapPoint();
            p.setDate(rs.getStatDate().toString());
            p.setMinutes(rs.getReadMinutes());
            points.add(p);
        }
        return points;
    }

    @Override
    public StatsOverviewVO getOverview(Integer userId) {
        StatsOverviewVO vo = new StatsOverviewVO();

        List<ReadingStats> all = list(new LambdaQueryWrapper<ReadingStats>()
                .eq(ReadingStats::getUserId, userId));
        long totalMinutes = 0;
        for (ReadingStats rs : all) totalMinutes += rs.getReadMinutes();
        vo.setTotalMinutes(totalMinutes);

        vo.setTotalBooks(bookMapper.selectCount(new LambdaQueryWrapper<Book>()
                .eq(Book::getUserId, userId)));

        vo.setStreakDays(calcStreak(all));
        LocalDate today = LocalDate.now();
        long todayMinutes = 0;
        for (ReadingStats rs : all) {
            if (today.equals(rs.getStatDate())) {
                todayMinutes = rs.getReadMinutes();
                break;
            }
        }
        vo.setTodayMinutes(todayMinutes);
        var user = userMapper.selectById(userId);
        vo.setDailyGoal(user != null && user.getDailyGoal() != null ? user.getDailyGoal() : 30);

        return vo;
    }

    @Override
    public int[] getWeeklyHabit(Integer userId) {
        List<ReadingStats> stats = list(new LambdaQueryWrapper<ReadingStats>()
                .eq(ReadingStats::getUserId, userId)
                .ge(ReadingStats::getStatDate, LocalDate.now().minusWeeks(12)));

        int[] dailyMinutes = new int[7];
        for (ReadingStats rs : stats) {
            int dow = rs.getStatDate().getDayOfWeek().getValue() - 1;
            dailyMinutes[dow] += rs.getReadMinutes();
        }
        return dailyMinutes;
    }

    private long calcStreak(List<ReadingStats> stats) {
        if (stats.isEmpty()) return 0;
        List<LocalDate> activeDays = new ArrayList<>();
        for (ReadingStats rs : stats) {
            if (rs.getReadMinutes() > 0) activeDays.add(rs.getStatDate());
        }
        activeDays.sort(Collections.reverseOrder());

        long streak = 0;
        LocalDate cursor = LocalDate.now();
        for (LocalDate d : activeDays) {
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

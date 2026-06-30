package com.moeread.dto;

import lombok.Data;

@Data
public class StatsOverviewVO {
    private long totalMinutes;
    private long totalBooks;
    private long streakDays;
}

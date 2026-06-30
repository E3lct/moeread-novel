package com.moeread.dto;

import lombok.Data;

/**
 * 热力图数据点
 */
@Data
public class HeatmapPoint {
    private String date;
    private int minutes;
}

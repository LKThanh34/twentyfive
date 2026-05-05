package com.project.DuAnTotNghiep.dto.Statistic;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthInYearStatistic {
    private int month;
    private BigDecimal revenue;
}

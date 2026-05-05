package com.project.DuAnTotNghiep.dto.Statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DayInMonthStatistic {
    private String date;
    private Double revenue;

}

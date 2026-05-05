package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.dto.Statistic.*;

import java.util.List;

public interface StatisticService {
    List<DayInMonthStatistic> getDayInMonthStatistic(String month, String year);

    List<MonthInYearStatistic> getMonthInYearStatistic(String year);

    List<MonthInYearStatistic2> getMonthlyRevenue(String fromDate, String toDate);
    List<BestSellerProduct> getBestSellerProduct(String fromDate, String toDate);

    List<BestSellerProduct> getBestSellerProductAll();


    List<DayInMonthStatistic2> getDailyRevenue2(String startDate, String endDate);

    List<ProductStatistic> getStatisticProductInTime(String fromDate, String toDate);

    List<OrderStatistic> getStatisticOrder();
}

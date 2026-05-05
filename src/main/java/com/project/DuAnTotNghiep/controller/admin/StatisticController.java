package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.dto.Statistic.BestSellerProduct;
import com.project.DuAnTotNghiep.dto.Statistic.DayInMonthStatistic;
import com.project.DuAnTotNghiep.dto.Statistic.MonthInYearStatistic;
import com.project.DuAnTotNghiep.dto.Statistic.UserStatistic;
import com.project.DuAnTotNghiep.repository.BillRepository;
import com.project.DuAnTotNghiep.service.AccountService;
import com.project.DuAnTotNghiep.service.StatisticService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Controller
public class StatisticController {

    private final AccountService accountService;

    private final BillRepository billRepository;

    private final StatisticService statisticService;

    public StatisticController(AccountService accountService, BillRepository billRepository, StatisticService statisticService) {
        this.accountService = accountService;
        this.billRepository = billRepository;
        this.statisticService = statisticService;
    }

    @GetMapping("/admin/thong-ke-doanh-thu")
    public String viewStatisticRevenuePage(Model model) {
        LocalDateTime currentDateTime = LocalDateTime.now();

        LocalDateTime firstDayOfWeek = currentDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        LocalDateTime lastDayOfWeek = firstDayOfWeek.plusDays(6).with(LocalTime.MAX);
        LocalDateTime firstDayOfMonth = currentDateTime.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime lastDayOfMonth = currentDateTime.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime startOfDay = currentDateTime.toLocalDate().atStartOfDay();
        String startOfDayStr = startOfDay.format(formatter);
        String firstDayOfWeekStr = firstDayOfWeek.format(formatter);
        String lastDayOfWeekStr = lastDayOfWeek.format(formatter);
        String firstDayOfMonthStr = firstDayOfMonth.format(formatter);
        String lastDayOfMonthStr = lastDayOfMonth.format(formatter);

        model.addAttribute("revenueAll", billRepository.calculateTotalRevenue());
        model.addAttribute("revenueWeek", billRepository.calculateTotalRevenueFromDate(firstDayOfWeekStr, lastDayOfWeekStr));
        model.addAttribute("revenueToday", billRepository.calculateTotalRevenueFromDate(startOfDayStr, currentDateTime.format(formatter)));
        model.addAttribute("revenueMonth", billRepository.calculateTotalRevenueFromDate(firstDayOfMonthStr, lastDayOfMonthStr));

        LocalDateTime yesterday = currentDateTime.minusDays(1);
        String yesterdayStr = yesterday.format(formatter);

        LocalDateTime lastWeekStart = currentDateTime.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        LocalDateTime lastWeekEnd = lastWeekStart.plusDays(6).with(LocalTime.MAX);
        String lastWeekStartStr = lastWeekStart.format(formatter);
        String lastWeekEndStr = lastWeekEnd.format(formatter);

        LocalDateTime lastMonthStart = currentDateTime.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime lastMonthEnd = lastMonthStart.plusMonths(1).minusDays(1).with(LocalTime.MAX);
        String lastMonthStartStr = lastMonthStart.format(formatter);
        String lastMonthEndStr = lastMonthEnd.format(formatter);

        // Adjustments for the previous day
        LocalDateTime yesterdayStartOfDay = currentDateTime.minusDays(1).toLocalDate().atStartOfDay();
        String yesterdayStartOfDayStr = yesterdayStartOfDay.format(formatter);
        String yesterdayEndOfDayStr = currentDateTime.with(LocalTime.MIN).format(formatter);

        Double revenueYesterday = billRepository.calculateTotalRevenueFromDate(yesterdayStartOfDayStr, yesterdayEndOfDayStr);
        Double revenueLastWeek = billRepository.calculateTotalRevenueFromDate(lastWeekStartStr, lastWeekEndStr);
        Double revenueLastMonth = billRepository.calculateTotalRevenueFromDate(lastMonthStartStr, lastMonthEndStr);

        Double revenueToday = (Double) model.getAttribute("revenueToday");
        Double revenueWeek = (Double) model.getAttribute("revenueWeek");
        Double revenueMonth = (Double) model.getAttribute("revenueMonth");

        Double percentageYesterday = calculatePercentage(revenueYesterday, revenueToday);
        Double percentageLastWeek = calculatePercentage(revenueLastWeek, revenueWeek);
        Double percentageLastMonth = calculatePercentage(revenueLastMonth, revenueMonth);

        model.addAttribute("percentageYesterday", percentageYesterday);
        model.addAttribute("percentageLastWeek", percentageLastWeek);
        model.addAttribute("percentageLastMonth", percentageLastMonth);
        model.addAttribute("bestSellers", statisticService.getBestSellerProductAll());

        return "/admin/thong-ke-doanh-thu";
    }

    @GetMapping("/admin/thong-ke-san-pham")
    public String viewStatisticProductPage(Model model) {

        return "/admin/thong-ke-san-pham";
    }

    private double calculatePercentage(double baseValue, double comparedValue) {
//        if(comparedValue == 0) {
//            return 0;
//        }

        if(baseValue == 0 && comparedValue > 0) {
            return 99;
        }

        if (baseValue == 0) {
            return 0; // Tránh chia cho 0
        }

        return ((comparedValue - baseValue) / baseValue) * 100;
    }
}

package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.Statistic.*;
import com.project.DuAnTotNghiep.entity.Bill;
import com.project.DuAnTotNghiep.repository.BillRepository;
import com.project.DuAnTotNghiep.repository.CustomerRepository;
import com.project.DuAnTotNghiep.repository.ProductRepository;
import com.project.DuAnTotNghiep.service.StatisticService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticServiceImpl implements StatisticService {

    private final BillRepository billRepository;
    private final ProductRepository productRepository;

    private final CustomerRepository customerRepository;

    public StatisticServiceImpl(BillRepository billRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.billRepository = billRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public List<DayInMonthStatistic> getDayInMonthStatistic(String month, String year) {
        List<Object[]> results = billRepository.statisticRevenueDayInMonth(month, year);
        List<DayInMonthStatistic> dayInMonthStatisticList = new ArrayList<>();

        List<LocalDate> allDatesInMonth = new ArrayList<>();
        YearMonth yearMonthObject = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
        int numDaysInMonth = yearMonthObject.lengthOfMonth();
        LocalDate startDate = yearMonthObject.atDay(1);
        LocalDate endDate = yearMonthObject.atDay(numDaysInMonth);
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            allDatesInMonth.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

// Điền vào doanh thu cho mỗi ngày
        Map<LocalDate, Double> revenueMap = new HashMap<>();
        for (Object[] result : results) {
            java.sql.Date sqlDate = (java.sql.Date) result[0];
            LocalDate date = sqlDate.toLocalDate();
            Double revenue = ((Number) result[1]).doubleValue();
            revenueMap.put(date, revenue);
        }

        for (LocalDate date : allDatesInMonth) {
            Double revenue = revenueMap.getOrDefault(date, 0.0);
            DayInMonthStatistic entry = new DayInMonthStatistic(date.toString().substring(5), revenue);
            dayInMonthStatisticList.add(entry);
        }
        return dayInMonthStatisticList;
    }

    public List<DayInMonthStatistic2> getDailyRevenue2(String startDate, String endDate) {
        LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


        List<Object[]> results = billRepository.statisticRevenueDaily(startDateTime.format(formatter), endDateTime.format(formatter));

        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();

// Iterate through all the days in the date range
        LocalDate currentDate = startDateTime.toLocalDate();
        while (!currentDate.isAfter(endDateTime.toLocalDate())) {
            result.put(currentDate, BigDecimal.ZERO);
            currentDate = currentDate.plusDays(1);
        }

// Update the revenue for days with orders
        for (Object[] object : results) {
            LocalDate orderDate = LocalDate.parse((String) object[0]);
            BigDecimal totalAmount = BigDecimal.valueOf((Double) object[1]);

            // Update the revenue for the specific day
            result.put(orderDate, result.getOrDefault(orderDate, BigDecimal.ZERO).add(totalAmount));
        }

// Now, convert the result to a sorted List<DayInMonthStatistic2>
        List<DayInMonthStatistic2> statistics = result.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DayInMonthStatistic2(entry.getKey().toString().substring(5), entry.getValue()))
                .collect(Collectors.toList());

        return statistics;
    }

    @Override
    public List<ProductStatistic> getStatisticProductInTime(String fromDate, String toDate) {
        return productRepository.getStatisticProduct(fromDate, toDate);
    }

    @Override
    public List<OrderStatistic> getStatisticOrder() {
        return billRepository.statisticOrder();
    }

    @Override
    public List<MonthInYearStatistic> getMonthInYearStatistic(String year) {
        int yearInt = Integer.parseInt(year);
        LocalDate startDate = LocalDate.of(yearInt, 1, 1);
        LocalDate endDate = LocalDate.of(yearInt, 12, 31);
        List<Object[]> results = billRepository.statisticRevenueMonthInYear(year);

        Map<Integer, BigDecimal> revenueMap = new HashMap<>();
        for (Object[] result : results) {
            int month = (Integer)result[0];
            BigDecimal totalAmount = BigDecimal.valueOf((Double) result[1]);

            revenueMap.put(month, revenueMap.getOrDefault(month, BigDecimal.ZERO).add(totalAmount));
        }

        List<MonthInYearStatistic> reportEntries = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal revenue = revenueMap.getOrDefault(month, BigDecimal.ZERO);
            MonthInYearStatistic entry = new MonthInYearStatistic(month, revenue);
            reportEntries.add(entry);
        }

        return reportEntries;
    }

    public List<MonthInYearStatistic2> getMonthlyRevenue(String fromDate, String toDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Parse input date strings
        LocalDate startDate = LocalDate.parse(fromDate + "-01", formatter);
        LocalDate endDate = LocalDate.parse(toDate + "-01", formatter).plusMonths(1).minusDays(1);

        List<LocalDate> monthRange = startDate.datesUntil(endDate.plusDays(1), java.time.Period.ofMonths(1)).collect(Collectors.toList());

        // Retrieve completed orders from the repository within the date range
        List<Object[]> results = billRepository.statisticRevenueFormMonth(startDate.format(formatter), endDate.format(formatter));

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM-yyyy");
        // Calculate revenue per month
        Map<String, BigDecimal> revenueMap = results.stream()
                .collect(Collectors.groupingBy(
                        result -> (String)result[0],
                        Collectors.reducing(BigDecimal.ZERO, result -> BigDecimal.valueOf((Double)result[1]), BigDecimal::add)
                ));

        return monthRange.stream()
                .map(month -> {
                    String monthYear = month.format(outputFormatter);
                    BigDecimal revenue = revenueMap.getOrDefault(monthYear, BigDecimal.ZERO);
                    return new MonthInYearStatistic2(monthYear, revenue);
                })
                .collect(Collectors.toList());
    }



    @Override
    public List<BestSellerProduct> getBestSellerProduct(String fromDate, String toDate) {
        return productRepository.getBestSellerProduct(fromDate, toDate);
    }

    public List<TopCustomerBuy> getTopCustomerBuy(String fromDate, String toDate) {
        return customerRepository.findTopCustomersByPurchases();
    }

    @Override
    public List<BestSellerProduct> getBestSellerProductAll() {
        return productRepository.getBestSellerProduct();
    }
}

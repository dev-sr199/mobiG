package com.ecommerce.project.service;

import com.ecommerce.project.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AdminDashboardService {

    @Autowired
    OrderService orderService;

    public List<Order> getAllOrdersToDashboard() {
        return orderService.getAllOrders();
    }

    public List<Order> getAllNonCancelledOrdersInReverseOrderToDashboard() {
        List<Order> userOrders = getAllOrdersToDashboard();
        List<Order> filteredUserOrders = new ArrayList<>(userOrders.stream().filter(order -> order.getOrderStatus().getId() != 6).toList());
        Collections.reverse(filteredUserOrders);
        return filteredUserOrders;
    }

    public List<LocalDate> getLocalDateList() {

        List<Order> filteredUserOrders = getAllNonCancelledOrdersInReverseOrderToDashboard();
        List<LocalDate> localDateList = new ArrayList<>();

        for (Order order : filteredUserOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList.add(localDate);
        }

        return localDateList;

    }

    public List<Order> getDailyOrders() {
        LocalDate today = LocalDate.now();
        List<Order> filteredUserOrders = getAllNonCancelledOrdersInReverseOrderToDashboard();
        List<LocalDate> localDateList = getLocalDateList();

        List<Order> dailyOrders = new ArrayList<>();

        for (int i=0; i<localDateList.size(); i++) {
            LocalDate localDate = localDateList.get(i);
            if (localDate.isEqual(today)) {
                dailyOrders.add(filteredUserOrders.get(i));
            }
        }

        return dailyOrders;
    }

    public List<Order> getWeeklyOrders() {
        LocalDate today = LocalDate.now();
        List<Order> filteredUserOrders = getAllNonCancelledOrdersInReverseOrderToDashboard();
        List<LocalDate> localDateList = getLocalDateList();

        List<Order> weeklyOrders = new ArrayList<>();

        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        for (int i=0; i<localDateList.size(); i++) {
            LocalDate localDate = localDateList.get(i);
            if (localDate.isAfter(startOfWeek.minusDays(1)) && localDate.isBefore(endOfWeek.plusDays(1))) {
                weeklyOrders.add(filteredUserOrders.get(i));
            }
        }

        return weeklyOrders;
    }

    public List<Order> getMonthlyOrders() {
        LocalDate today = LocalDate.now();
        List<Order> filteredUserOrders = getAllNonCancelledOrdersInReverseOrderToDashboard();
        List<LocalDate> localDateList = getLocalDateList();

        List<Order> monthlyOrders = new ArrayList<>();

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        for (int i=0; i<localDateList.size(); i++) {
            LocalDate localDate = localDateList.get(i);
            if (localDate.isAfter(startOfMonth.minusDays(1)) && localDate.isBefore(endOfMonth.plusDays(1))) {
                monthlyOrders.add(filteredUserOrders.get(i));
            }
        }

        return monthlyOrders;
    }

    public List<Order> getYearlyOrders() {
        LocalDate today = LocalDate.now();
        List<Order> filteredUserOrders = getAllNonCancelledOrdersInReverseOrderToDashboard();
        List<LocalDate> localDateList = getLocalDateList();

        List<Order> yearlyOrders = new ArrayList<>();

        LocalDate startOfYear = today.withDayOfYear(1);
        LocalDate endOfYear = today.withDayOfYear(today.lengthOfYear());

        for (int i=0; i<localDateList.size(); i++) {
            LocalDate localDate = localDateList.get(i);
            if (localDate.isAfter(startOfYear.minusDays(1)) && localDate.isBefore(endOfYear.plusDays(1))) {
                yearlyOrders.add(filteredUserOrders.get(i));
            }
        }

        return yearlyOrders;
    }

}

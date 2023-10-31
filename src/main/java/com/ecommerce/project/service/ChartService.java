package com.ecommerce.project.service;

import com.ecommerce.project.entity.Category;
import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class ChartService {

    @Autowired
    OrderService orderService;

    @Autowired
    CategoryService categoryService;

    public List<List<Object>> getChartDataWeeklyOrders() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDate sixDaysAgo = today.minusDays(6);

        for (int i=0; i<localDateList.size(); i++) {
            LocalDate localDate = localDateList.get(i);
            if (!localDate.isBefore(sixDaysAgo)) {
                modelOrders.add(validOrders.get(i));
            }
        }

        DayOfWeek dayToday = today.getDayOfWeek();

        List<List<Object>> rowsInGraph = new ArrayList<>();

        Map<DayOfWeek, Integer> mapOfDayOfWeekAndOrders = new HashMap<>();

        for (int i=0;i<7;i++) {
            mapOfDayOfWeekAndOrders.put(dayToday.minus(i), 0);
        }

        for (Order order : modelOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOfDayOfWeekAndOrders.put(localDate.getDayOfWeek(), mapOfDayOfWeekAndOrders.get(localDate.getDayOfWeek()) + 1);
        }

        for (int i=6;i>=0;i--) {
            rowsInGraph.add(Arrays.asList(dayToday.minus(i).toString(),mapOfDayOfWeekAndOrders.get(dayToday.minus(i))));
        }

        return rowsInGraph;
    }

    public List<List<Object>> getChartDataWeeklyRevenue() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDate sixDaysAgo = today.minusDays(6);

        for (int i=0; i<localDateList.size(); i++) {
            LocalDate localDate = localDateList.get(i);
            if (!localDate.isBefore(sixDaysAgo)) {
                modelOrders.add(validOrders.get(i));
            }
        }

        DayOfWeek dayToday = today.getDayOfWeek();

        List<List<Object>> rowsInGraph = new ArrayList<>();

        Map<DayOfWeek, Double> mapOfDayOfWeekAndRevenue = new HashMap<>();

        for (int i=0;i<7;i++) {
            mapOfDayOfWeekAndRevenue.put(dayToday.minus(i), 0.0);
        }

        for (Order order : modelOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOfDayOfWeekAndRevenue.put(localDate.getDayOfWeek(), mapOfDayOfWeekAndRevenue.get(localDate.getDayOfWeek()) + order.getTotalPrice());
        }

        for (int i=6;i>=0;i--) {
            rowsInGraph.add(Arrays.asList(dayToday.minus(i).toString(),mapOfDayOfWeekAndRevenue.get(dayToday.minus(i))));
        }

        return rowsInGraph;
    }

    public List<List<Object>> getChartDataMonthlyOrders() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusDays(29);

        for (int i=0; i<localDateList2.size(); i++) {
            LocalDate localDate = localDateList2.get(i);
            if (!localDate.isBefore(monthAgo)) {
                modelOrders.add(validOrders.get(i));
            }
        }

        int dayToday = today.getDayOfMonth();
        Month monthToday = today.getMonth();
        String label = dayToday + " " + monthToday.name().substring(0,3);

        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();


        Map<String, Integer> mapOrdersMonthly = new HashMap<>();

        for (int i=0;i<30;i++) {
            mapOrdersMonthly.put(today.minusDays(i).getDayOfMonth() + " " + today.minusDays(i).getMonth().name().substring(0, 3), 0);
        }

        for (Order order : modelOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersMonthly.put(localDate.getDayOfMonth() + " " + localDate.getMonth().name().substring(0, 3), mapOrdersMonthly.get(localDate.getDayOfMonth() + " " + localDate.getMonth().name().substring(0, 3)) + 1);
        }

        for (int i=29;i>=0;i--) {
            rowsInGraph.add(today.minusDays(i).getDayOfMonth() + " " + today.minusDays(i).getMonth().name().substring(0, 3));
            rowsInGraph2.add(mapOrdersMonthly.get(today.minusDays(i).getDayOfMonth() + " " + today.minusDays(i).getMonth().name().substring(0, 3)));
        }

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<List<Object>> getChartDataMonthlyRevenue() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusDays(29);

        for (int i=0; i<localDateList2.size(); i++) {
            LocalDate localDate = localDateList2.get(i);
            if (!localDate.isBefore(monthAgo)) {
                modelOrders.add(validOrders.get(i));
            }
        }

        int dayToday = today.getDayOfMonth();
        Month monthToday = today.getMonth();
        String label = dayToday + " " + monthToday.name().substring(0,3);

        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();


        Map<String, Double> mapOrdersMonthly = new HashMap<>();

        for (int i=0;i<30;i++) {
            mapOrdersMonthly.put(today.minusDays(i).getDayOfMonth() + " " + today.minusDays(i).getMonth().name().substring(0, 3), 0.0);
        }

        for (Order order : modelOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersMonthly.put(localDate.getDayOfMonth() + " " + localDate.getMonth().name().substring(0, 3), mapOrdersMonthly.get(localDate.getDayOfMonth() + " " + localDate.getMonth().name().substring(0, 3)) + order.getTotalPrice());
        }

        for (int i=29;i>=0;i--) {
            rowsInGraph.add(today.minusDays(i).getDayOfMonth() + " " + today.minusDays(i).getMonth().name().substring(0, 3));
            rowsInGraph2.add(mapOrdersMonthly.get(today.minusDays(i).getDayOfMonth() + " " + today.minusDays(i).getMonth().name().substring(0, 3)));
        }

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<List<Object>> getChartDataYearlyOrders() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDate yearAgo = today.minusMonths(11);

        for (int i=0; i<localDateList2.size(); i++) {
            LocalDate localDate = localDateList2.get(i);
            if (!localDate.isBefore(yearAgo)) {
                modelOrders.add(validOrders.get(i));
            }
        }

        int dayToday = today.getDayOfMonth();
        Month monthToday = today.getMonth();
        String label = dayToday + " " + monthToday.name().substring(0,3);

        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();


        Map<String, Integer> mapOrdersMonthly = new HashMap<>();

        for (int i=0;i<12;i++) {
            mapOrdersMonthly.put(today.minusMonths(i).getMonth().name(), 0);
        }

        for (Order order : modelOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersMonthly.put(localDate.getMonth().name(), mapOrdersMonthly.get(localDate.getMonth().name()) + 1);
        }

        for (int i=0;i<12;i++) {
            rowsInGraph.add(today.minusMonths(i).getMonth().name());
            rowsInGraph2.add(mapOrdersMonthly.get(today.minusMonths(i).getMonth().name()));
        }

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<List<Object>> getChartDataYearlyRevenue() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDate yearAgo = today.minusMonths(11);

        for (int i=0; i<localDateList2.size(); i++) {
            LocalDate localDate = localDateList2.get(i);
            if (!localDate.isBefore(yearAgo)) {
                modelOrders.add(validOrders.get(i));
            }
        }

        int dayToday = today.getDayOfMonth();
        Month monthToday = today.getMonth();
        String label = dayToday + " " + monthToday.name().substring(0,3);

        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();


        Map<String, Double> mapOrdersMonthly = new HashMap<>();

        for (int i=0;i<12;i++) {
            mapOrdersMonthly.put(today.minusMonths(i).getMonth().name(), 0.0);
        }

        for (Order order : modelOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersMonthly.put(localDate.getMonth().name(), mapOrdersMonthly.get(localDate.getMonth().name()) + order.getTotalPrice());
        }

        for (int i=0;i<12;i++) {
            rowsInGraph.add(today.minusMonths(i).getMonth().name());
            rowsInGraph2.add(mapOrdersMonthly.get(today.minusMonths(i).getMonth().name()));
        }

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<Object> getChartDataDailyOrders() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDateTime> localDateList2 = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDateTime localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            localDateList2.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDateTime todayTime = LocalDateTime.now();

        for (int i=0; i<localDateList2.size(); i++) {
            LocalDate localDate = localDateList2.get(i).toLocalDate();
            if (localDate.isEqual(today)) {
                modelOrders.add(validOrders.get(i));
            }
        }

        List<Object> rowsInGraph = new ArrayList<>();

        Map<Integer, Integer> mapOrdersDaily = new HashMap<>();

        for (int i=0;i<24;i++) {
            mapOrdersDaily.put(i, 0);
        }

        for (Order order : modelOrders) {
            LocalDateTime localDateTime = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            mapOrdersDaily.put(localDateTime.getHour(), mapOrdersDaily.get(localDateTime.getHour()) + 1);
        }

        for (int i=0;i<24;i++) {
            rowsInGraph.add(mapOrdersDaily.get(i));
        }

        return rowsInGraph;
    }

    public List<Object> getChartDataDailyRevenue() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDateTime> localDateList2 = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDateTime localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            localDateList2.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDateTime todayTime = LocalDateTime.now();

        for (int i=0; i<localDateList2.size(); i++) {
            LocalDate localDate = localDateList2.get(i).toLocalDate();
            if (localDate.isEqual(today)) {
                modelOrders.add(validOrders.get(i));
            }
        }

        List<Object> rowsInGraph = new ArrayList<>();

        Map<Integer, Double> mapOrdersDaily = new HashMap<>();

        for (int i=0;i<24;i++) {
            mapOrdersDaily.put(i, 0.0);
        }

        for (Order order : modelOrders) {
            LocalDateTime localDateTime = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            mapOrdersDaily.put(localDateTime.getHour(), mapOrdersDaily.get(localDateTime.getHour()) + order.getTotalPrice());
        }

        for (int i=0;i<24;i++) {
            rowsInGraph.add(mapOrdersDaily.get(i));
        }

        return rowsInGraph;
    }

    public List<List<Object>> getLastFiveYears() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDate fiveYearsAgo = today.minusYears(4);

        for (int i=0; i<localDateList2.size(); i++) {
            LocalDate localDate = localDateList2.get(i);
            if (!localDate.isBefore(fiveYearsAgo)) {
                modelOrders.add(validOrders.get(i));
            }
        }


        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();


        Map<Integer, Integer> mapOrdersFive = new HashMap<>();

        for (int i=0;i<5;i++) {
            mapOrdersFive.put(today.minusYears(i).getYear(), 0);
        }

        for (Order order : modelOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersFive.put(localDate.getYear(), mapOrdersFive.get(localDate.getYear()) + 1);
        }

        for (int i=0;i<5;i++) {
            rowsInGraph.add(today.minusYears(i).getYear());
            rowsInGraph2.add(mapOrdersFive.get(today.minusYears(i).getYear()));
        }

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<List<Object>> getLastFiveYears2() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        LocalDate today = LocalDate.now();
        LocalDate fiveYearsAgo = today.minusYears(4);

        for (int i=0; i<localDateList2.size(); i++) {
            LocalDate localDate = localDateList2.get(i);
            if (!localDate.isBefore(fiveYearsAgo)) {
                modelOrders.add(validOrders.get(i));
            }
        }


        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();


        Map<Integer, Double> mapOrdersFive = new HashMap<>();

        for (int i=0;i<5;i++) {
            mapOrdersFive.put(today.minusYears(i).getYear(), 0.0);
        }

        for (Order order : modelOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersFive.put(localDate.getYear(), mapOrdersFive.get(localDate.getYear()) + order.getTotalPrice());
        }

        for (int i=0;i<5;i++) {
            rowsInGraph.add(today.minusYears(i).getYear());
            rowsInGraph2.add(mapOrdersFive.get(today.minusYears(i).getYear()));
        }

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<List<Object>> getLineChart() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();

        Map<String, Integer> mapOrdersMonthly = new HashMap<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersMonthly.put(localDate.getMonth().name().substring(0, 3) + " " + localDate.getYear(), 0);
        }

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersMonthly.put(localDate.getMonth().name().substring(0, 3) + " " + localDate.getYear(), mapOrdersMonthly.get(localDate.getMonth().name().substring(0, 3) + " " + localDate.getYear()) + 1);
        }

        for (int i=0; i<validOrders.size(); i++) {
            LocalDate localDate = validOrders.get(i).getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (i!=0 && localDate.getMonth().equals(validOrders.get(i-1).getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonth())) {
                continue;
            }
            rowsInGraph.add(localDate.getMonth().name().substring(0, 3) + " " + localDate.getYear());
            rowsInGraph2.add(mapOrdersMonthly.get(localDate.getMonth().name().substring(0, 3) + " " + localDate.getYear()));
        }

        Collections.reverse(rowsInGraph);
        Collections.reverse(rowsInGraph2);

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<List<Object>> getLineChart2() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();

        Map<String, Double> mapOrdersMonthly = new HashMap<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersMonthly.put(localDate.getMonth().name().substring(0, 3) + " " + localDate.getYear(), 0.0);
        }

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersMonthly.put(localDate.getMonth().name().substring(0, 3) + " " + localDate.getYear(), mapOrdersMonthly.get(localDate.getMonth().name().substring(0, 3) + " " + localDate.getYear()) + order.getTotalPrice());
        }

        for (int i=0; i<validOrders.size(); i++) {
            LocalDate localDate = validOrders.get(i).getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (i!=0 && localDate.getMonth().equals(validOrders.get(i-1).getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonth())) {
                continue;
            }
            rowsInGraph.add(localDate.getMonth().name().substring(0, 3) + " " + localDate.getYear());
            rowsInGraph2.add(mapOrdersMonthly.get(localDate.getMonth().name().substring(0, 3) + " " + localDate.getYear()));
        }

        Collections.reverse(rowsInGraph);
        Collections.reverse(rowsInGraph2);

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<List<Object>> getLineChartDaily1() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();

        Map<String, Integer> mapOrdersMonthly = new HashMap<>();

        LocalDate today = LocalDate.now();

        LocalDate nostalgia = LocalDate.now();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (localDate.isBefore(nostalgia)) {
                nostalgia = localDate;
            }
        }

        int ic = 0;
        while (today.minusDays(ic).isAfter(nostalgia) || today.minusDays(ic).isEqual(nostalgia)) {
            mapOrdersMonthly.put(String.valueOf(today.minusDays(ic)), 0);
            ic++;
        }

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersMonthly.put(String.valueOf(localDate), mapOrdersMonthly.get(String.valueOf(localDate)) + 1);
        }

        int ic2 = 0;
        while (today.minusDays(ic2).isAfter(nostalgia) || today.minusDays(ic2).isEqual(nostalgia)) {
            rowsInGraph.add(String.valueOf(today.minusDays(ic2)));
            rowsInGraph2.add(mapOrdersMonthly.get(String.valueOf(today.minusDays(ic2))));
            ic2++;
        }

        Collections.reverse(rowsInGraph);
        Collections.reverse(rowsInGraph2);

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }
    public List<List<Object>> getLineChartDaily2() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<LocalDate> localDateList2 = new ArrayList<>();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList2.add(localDate);
        }

        List<Object> rowsInGraph = new ArrayList<>();
        List<Object> rowsInGraph2 = new ArrayList<>();

        Map<String, Double> mapOrdersMonthly = new HashMap<>();

        LocalDate today = LocalDate.now();

        LocalDate nostalgia = LocalDate.now();

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (localDate.isBefore(nostalgia)) {
                nostalgia = localDate;
            }
        }

        int ic = 0;
        while (today.minusDays(ic).isAfter(nostalgia) || today.minusDays(ic).isEqual(nostalgia)) {
            mapOrdersMonthly.put(String.valueOf(today.minusDays(ic)), 0.0);
            ic++;
        }

        for (Order order : validOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            mapOrdersMonthly.put(String.valueOf(localDate), mapOrdersMonthly.get(String.valueOf(localDate)) + order.getTotalPrice());
        }

        int ic2 = 0;
        while (today.minusDays(ic2).isAfter(nostalgia) || today.minusDays(ic2).isEqual(nostalgia)) {
            rowsInGraph.add(String.valueOf(today.minusDays(ic2)));
            rowsInGraph2.add(mapOrdersMonthly.get(String.valueOf(today.minusDays(ic2))));
            ic2++;
        }

        Collections.reverse(rowsInGraph);
        Collections.reverse(rowsInGraph2);

        List<List<Object>> returnList = Arrays.asList(rowsInGraph, rowsInGraph2);
        return returnList;
    }

    public List<List<Object>> getPieChart() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<Category> categoryList = categoryService.getAllCategories();

        Map<Category, Integer> categoryIntegerMap = new HashMap<>();

        for (Category category : categoryList) {
            categoryIntegerMap.put(category, 0);
        }

        for (Order order : validOrders) {
            List<OrderItem> orderItemList = order.getOrderItems();
            for (OrderItem orderItem : orderItemList) {
                Category category = orderItem.getProduct().getCategory();
                categoryIntegerMap.put(category, categoryIntegerMap.get(category) + orderItem.getQuantity());
            }
        }

        List<List<Object>> rowsInGraph = new ArrayList<>();

        for (Category category : categoryList) {
            rowsInGraph.add(Arrays.asList(category.getName(),categoryIntegerMap.get(category)));
        }

        return rowsInGraph;
    }

    public List<List<Object>> getPieChart2() {
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> validOrders = new ArrayList<>(allOrders.stream().filter(order->order.getOrderStatus().getId()!=6).toList());
        Collections.reverse(validOrders);

        List<Category> categoryList = categoryService.getAllCategories();

        Map<Category, Double> categoryDoubleMap = new HashMap<>();

        for (Category category : categoryList) {
            categoryDoubleMap.put(category, 0.0);
        }

        for (Order order : validOrders) {
            List<OrderItem> orderItemList = order.getOrderItems();
            for (OrderItem orderItem : orderItemList) {
                Category category = orderItem.getProduct().getCategory();
                categoryDoubleMap.put(category, categoryDoubleMap.get(category) + orderItem.getSubtotal());
            }
        }

        List<List<Object>> rowsInGraph = new ArrayList<>();

        for (Category category : categoryList) {
            rowsInGraph.add(Arrays.asList(category.getName(),categoryDoubleMap.get(category)));
        }

        return rowsInGraph;
    }

}

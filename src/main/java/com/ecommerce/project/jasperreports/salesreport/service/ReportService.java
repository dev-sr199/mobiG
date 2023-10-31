package com.ecommerce.project.jasperreports.salesreport.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.OrderItem;
import com.ecommerce.project.jasperreports.salesreport.entity.Report;
import com.ecommerce.project.service.OrderService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AmazonS3 s3Client;

    @Value("${application.bucket.name}")
    private String bucketName;

    public byte[] exportPdfReport(String format) throws FileNotFoundException, JRException {

        List<Order> userOrders = orderService.getAllOrders();
        List<Order> filteredUserOrders = new ArrayList<>(userOrders.stream().filter(order -> order.getOrderStatus().getId() != 6).toList());
        Collections.reverse(filteredUserOrders);

        List<Report> reportList = new ArrayList<>();

        for (Order order : filteredUserOrders) {
            Report report = new Report();
            report.setOrderId(order.getId());
            report.setOrderDate(order.getOrderDate());
            report.setCustomer(order.getUser().getEmail());
            report.setTotalAmount(order.getTotalPrice());
            report.setPaymentMethod(order.getPaymentMethod().getMethod());
            List<OrderItem> orderItemList = order.getOrderItems();

            for (int i=0; i<orderItemList.size();i++) {
                if (i==0)
                    report.setProducts(orderItemList.get(i).getProduct().getName() + " - " + orderItemList.get(i).getQuantity() + " nos.");
                else
                    report.setProducts(report.getProducts() + ", " + orderItemList.get(i).getProduct().getName() + " - " + orderItemList.get(i).getQuantity() + " nos.");
            }
            report.setOrderStatus(order.getOrderStatus().getStatus());
            reportList.add(report);
        }

        //Load jrxml file and compile it
        S3Object s3Object = s3Client.getObject(bucketName, "salesreport1.jrxml");
        InputStream jrxmlInputStream = s3Object.getObjectContent();
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlInputStream);

        //Map resource to file
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportList);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("grandTotal", Math.round(filteredUserOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum)*100.0)/100.0);
        parameters.put("quantity", filteredUserOrders.stream().map(Order::getTotalQuantity).reduce(0, Integer::sum));
        parameters.put("totalOrders", filteredUserOrders.size());
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        //Export to pdf
        if (format.equalsIgnoreCase("pdf")) {
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            return pdfBytes;
        }

        //Export to xls
        if (format.equalsIgnoreCase("xlsx")) {

            JRBeanCollectionDataSource dataSource2 = new JRBeanCollectionDataSource(reportList);
            parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);
            JasperPrint jasperPrint2 = JasperFillManager.fillReport(jasperReport, parameters, dataSource2);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint2);
            exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, outputStream);
            exporter.exportReport();
            return outputStream.toByteArray();
        }

        return null;
    }

    public byte[] exportPdfReportForDate(LocalDate startDate,
                                         LocalDate endDate,
                                         String format) throws FileNotFoundException, JRException {

        List<Order> userOrders = orderService.getAllOrders();
        List<Order> filteredUserOrders = new ArrayList<>(userOrders.stream().filter(order -> order.getOrderStatus().getId() != 6).toList());
        Collections.reverse(filteredUserOrders);
        List<Order> dateOrders = new ArrayList<>();
        for (Order order : filteredUserOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (localDate.isAfter(startDate.minusDays(1)) && localDate.isBefore(endDate.plusDays(1))) {
                dateOrders.add(order);
            }
        }

        List<Report> reportList = new ArrayList<>();

        for (Order order : dateOrders) {
            Report report = new Report();
            report.setOrderId(order.getId());
            report.setOrderDate(order.getOrderDate());
            report.setCustomer(order.getUser().getEmail());
            report.setTotalAmount(order.getTotalPrice());
            report.setPaymentMethod(order.getPaymentMethod().getMethod());
            List<OrderItem> orderItemList = order.getOrderItems();

            for (int i=0; i<orderItemList.size();i++) {
                if (i==0)
                    report.setProducts(orderItemList.get(i).getProduct().getName() + " - " + orderItemList.get(i).getQuantity() + " nos.");
                else
                    report.setProducts(report.getProducts() + ", " + orderItemList.get(i).getProduct().getName() + " - " + orderItemList.get(i).getQuantity() + " nos.");
            }
            report.setOrderStatus(order.getOrderStatus().getStatus());
            reportList.add(report);
        }

        //Load jrxml file and compile it
        S3Object s3Object = s3Client.getObject(bucketName, "salesreport1.jrxml");
        InputStream jrxmlInputStream = s3Object.getObjectContent();
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlInputStream);

        //Map resource to file
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportList);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("grandTotal", Math.round(dateOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum)*100.0)/100.0);
        parameters.put("quantity", dateOrders.stream().map(Order::getTotalQuantity).reduce(0, Integer::sum));
        parameters.put("totalOrders", dateOrders.size());
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        //Export to pdf
        if (format.equalsIgnoreCase("pdf")) {
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            return pdfBytes;
        }

        //Export to xls
        if (format.equalsIgnoreCase("xlsx")) {

            JRBeanCollectionDataSource dataSource2 = new JRBeanCollectionDataSource(reportList);
            parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);
            JasperPrint jasperPrint2 = JasperFillManager.fillReport(jasperReport, parameters, dataSource2);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint2);
            exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, outputStream);
            exporter.exportReport();
            return outputStream.toByteArray();
        }

        return null;
    }


}

















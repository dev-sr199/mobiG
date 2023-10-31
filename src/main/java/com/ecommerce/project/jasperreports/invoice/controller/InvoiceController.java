package com.ecommerce.project.jasperreports.invoice.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.OrderItem;
import com.ecommerce.project.jasperreports.invoice.entity.InvoiceDataset;
import com.ecommerce.project.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import jdk.dynalink.linker.LinkerServices;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class InvoiceController {

    @Autowired
    OrderService orderService;

    @Autowired
    private AmazonS3 s3Client;

    @Value("${application.bucket.name}")
    private String bucketName;

    @GetMapping("/invoice/{id}")
    public void generateInvoice(@PathVariable Long id,
                                HttpServletResponse response) throws IOException, JRException {

        Order order = orderService.getOrderById(id);

        List<InvoiceDataset> invoiceDatasetList = new ArrayList<>();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("customerName", order.getUser().getFirstName() + " " + order.getUser().getLastName());
        parameters.put("customerEmail", order.getUser().getEmail());
        parameters.put("customerAddress", order.getAddress().getFullAddress());
        parameters.put("orderId", order.getId());
        parameters.put("orderDate", order.getOrderDate());
        parameters.put("orderStatus", order.getOrderStatus().getStatus());
        parameters.put("paymentMethod", order.getPaymentMethod().getMethod());
        parameters.put("total", order.getTotalPrice());

        List<OrderItem> orderItemList = order.getOrderItems();

        double xtotal = 0.0;
        for (OrderItem orderItem : orderItemList) {
            InvoiceDataset invoiceDataset = new InvoiceDataset();
            invoiceDataset.setProductName(orderItem.getProduct().getName());
            invoiceDataset.setQuantity(orderItem.getQuantity());
            invoiceDataset.setUnitPrice(orderItem.getProduct().getPrice());
            invoiceDataset.setStotal(orderItem.getSubtotal());

            invoiceDatasetList.add(invoiceDataset);

            xtotal+=orderItem.getSubtotal();
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(invoiceDatasetList);

        parameters.put("invoiceDataset", dataSource);

        if (xtotal==order.getTotalPrice())
            parameters.put("couponApplied", "NO");
        else
            parameters.put("couponApplied", "YES");

        //Load jrxml file and compile it
        S3Object s3Object = s3Client.getObject(bucketName, "invoice.jrxml");
        InputStream jrxmlInputStream = s3Object.getObjectContent();
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlInputStream);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

        response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");
        response.setContentType("application/pdf");

        response.getOutputStream().write(pdfBytes);

    }

}



















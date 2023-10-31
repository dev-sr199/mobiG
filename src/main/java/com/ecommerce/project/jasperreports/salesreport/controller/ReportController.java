package com.ecommerce.project.jasperreports.salesreport.controller;

import com.ecommerce.project.jasperreports.salesreport.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;

@Controller
public class ReportController {

    @Autowired
    ReportService reportService;

    @GetMapping("/generateReport/{format}")
    public void generateReport(@PathVariable String format, HttpServletResponse response) throws JRException, FileNotFoundException {

        byte[] reportData = reportService.exportPdfReport(format);

        response.setHeader("Content-Disposition", "attachment; filename=salesreport." + format);
        response.setContentType("application/" + format);

        try (OutputStream os = response.getOutputStream()) {
            os.write(reportData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/downloadReportDate")
    public void downloadReportDate(@ModelAttribute("startDate") LocalDate startDate,
                                   @ModelAttribute("endDate") LocalDate endDate,
                                   @ModelAttribute("format") String format,
                                   HttpServletResponse response) throws JRException, FileNotFoundException {

        byte[] reportData = reportService.exportPdfReportForDate(startDate, endDate, format);

        response.setHeader("Content-Disposition", "attachment; filename=salesreport." + format);
        response.setContentType("application/" + format);

        try (OutputStream os = response.getOutputStream()) {
            os.write(reportData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

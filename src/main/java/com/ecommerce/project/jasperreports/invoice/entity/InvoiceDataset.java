package com.ecommerce.project.jasperreports.invoice.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDataset {

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double stotal;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getStotal() {
        return stotal;
    }

    public void setStotal(Double stotal) {
        this.stotal = stotal;
    }
}

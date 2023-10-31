package com.ecommerce.project.dto;

import com.ecommerce.project.entity.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private int categoryId;

    @Min(value = 0, message = "Price cannot be negative")
    private double price;

    @PositiveOrZero(message = "Quantity cannot be negative")
    private long quantity;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    private String imageName;

}

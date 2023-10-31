package com.ecommerce.project.repository;

import com.ecommerce.project.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    void deleteAllByProductId(Long id);
}

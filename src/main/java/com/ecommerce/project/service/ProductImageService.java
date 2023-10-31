package com.ecommerce.project.service;

import com.ecommerce.project.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImageService {

    @Autowired
    ProductImageRepository productImageRepository;

    @Transactional
    public void deleteAllByProductId(Long id) {
        productImageRepository.deleteAllByProductId(id);
    }


}

package com.ecommerce.project.service;

import com.ecommerce.project.entity.PaymentMethod;
import com.ecommerce.project.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentMethodService {

    @Autowired
    PaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findAll();
    }

    public PaymentMethod getPaymentMethodById(int id) {
        return paymentMethodRepository.findById(id).get();
    }


}

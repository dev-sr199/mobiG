package com.ecommerce.project.service;

import com.ecommerce.project.entity.OrderStatus;
import com.ecommerce.project.repository.OrderStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderStatusService {

    @Autowired
    OrderStatusRepository orderStatusRepository;

    public OrderStatus findById(int id) {
        return orderStatusRepository.findById(id).get();
    }

    public List<OrderStatus> findAll() {
        return orderStatusRepository.findAll();
    }

}

package com.ecommerce.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "customer_order")
public class Order implements Comparable<Order>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;

    private double totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "order_status_id")
    private OrderStatus orderStatus;

    @OneToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    public Order() {
    }

    @Override
    public int compareTo(Order o) {
        if (getOrderDate() == null || o.getOrderDate() == null) {
            return 0;
        }
        return getOrderDate().compareTo(o.getOrderDate());
    }

    public int getTotalQuantity() {
        return orderItems.stream().map(OrderItem::getQuantity).reduce(0, Integer::sum);
    }
}


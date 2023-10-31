package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    Optional<Coupon> findCouponByCouponCode(String couponCode);
}

package com.example.couponservice.repository;

import com.example.couponservice.entity.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    boolean existsByCouponIdAndUserUuid(Long couponId, UUID userUuid);
}
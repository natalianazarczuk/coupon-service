package com.example.couponservice.dto;

import com.example.couponservice.entity.Coupon;
import java.time.Instant;

public record CouponResponse(
        Long id,
        String code,
        Instant createdAt,
        int maxUsages,
        int currentUsages,
        String country
) {

    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getCreatedAt(),
                coupon.getMaxUsages(),
                coupon.getCurrentUsages(),
                coupon.getCountry()
        );
    }
}
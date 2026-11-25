package com.example.couponservice.service;

import com.example.couponservice.dto.CreateCouponRequest;
import com.example.couponservice.dto.CouponResponse;
import com.example.couponservice.entity.Coupon;
import com.example.couponservice.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponResponse createCoupon(CreateCouponRequest request) {
        var coupon = new Coupon(
                request.code(),
                Instant.now(),
                request.maxUsages(),
                request.country()
        );

        var savedCoupon = couponRepository.save(coupon);
        return CouponResponse.from(savedCoupon);
    }
}
package com.example.couponservice.service;

import com.example.couponservice.dto.CreateCouponRequest;
import com.example.couponservice.dto.CouponResponse;
import com.example.couponservice.dto.RedeemCouponRequest;
import com.example.couponservice.entity.Coupon;
import com.example.couponservice.entity.CouponRedemption;
import com.example.couponservice.exception.*;
import com.example.couponservice.repository.CouponRedemptionRepository;
import com.example.couponservice.repository.CouponRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;

    public CouponResponse createCoupon(CreateCouponRequest request) {
        var coupon = new Coupon(
                request.code(),
                Instant.now(),
                request.maxUsages(),
                request.country()
        );

        try {
            var savedCoupon = couponRepository.saveAndFlush(coupon);
            return CouponResponse.from(savedCoupon);
        } catch (DataIntegrityViolationException e) {
            throw new CouponAlreadyExistsException(request.code(), e);
        }
    }

    @Transactional
    public CouponResponse redeemCoupon(RedeemCouponRequest request, String userCountry) {
        var coupon = couponRepository.findByCodeForUpdate(request.code())
                .orElseThrow(() -> new CouponNotFoundException(request.code()));

        if (!coupon.getCountry().equalsIgnoreCase(userCountry)) {
            throw new CouponCountryMismatchException(request.code());
        }

        if (couponRedemptionRepository.existsByCouponIdAndUserUuid(
                coupon.getId(), request.userUuid())) {
            throw new CouponAlreadyRedeemedException(request.code());
        }

        if (coupon.isExhausted()) {
            throw new CouponExhaustedException(request.code());
        }

        coupon.incrementUsages();

        var redemption = new CouponRedemption(
                coupon.getId(),
                request.userUuid(),
                Instant.now()
        );

        couponRedemptionRepository.save(redemption);

        return CouponResponse.from(coupon);
    }
}
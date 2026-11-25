package com.example.couponservice.exception;

public class CouponAlreadyRedeemedException extends RuntimeException {

    public CouponAlreadyRedeemedException(String code) {
        super("Coupon with code '%s' has already been redeemed by this user".formatted(code));
    }
}

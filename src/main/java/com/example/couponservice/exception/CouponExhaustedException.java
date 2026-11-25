package com.example.couponservice.exception;

public class CouponExhaustedException extends RuntimeException {

    public CouponExhaustedException(String code) {
        super("Coupon with code '%s' has reached its usage limit".formatted(code));
    }
}

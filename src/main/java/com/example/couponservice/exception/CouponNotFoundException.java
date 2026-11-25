package com.example.couponservice.exception;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(String code) {
        super("Coupon with code '%s' does not exist".formatted(code));
    }
}

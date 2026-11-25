package com.example.couponservice.exception;

public class CouponAlreadyExistsExcpetion extends RuntimeException {

    public CouponAlreadyExistsExcpetion(String code, Throwable cause) {
        super("Coupon with code '%s' already exists".formatted(code), cause);
    }
}
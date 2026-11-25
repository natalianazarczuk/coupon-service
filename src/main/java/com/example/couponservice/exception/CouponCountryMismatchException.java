package com.example.couponservice.exception;

public class CouponCountryMismatchException extends RuntimeException {

    public CouponCountryMismatchException(String code) {
        super("Coupon with code '%s' is not available in this country".formatted(code));
    }
}
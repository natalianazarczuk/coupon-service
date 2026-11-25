package com.example.couponservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CouponAlreadyExistsExcpetion extends RuntimeException {

    public CouponAlreadyExistsExcpetion(String code, Throwable cause) {
        super("Coupon with code '%s' already exists".formatted(code), cause);
    }
}
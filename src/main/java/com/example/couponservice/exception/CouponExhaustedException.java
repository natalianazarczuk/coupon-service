package com.example.couponservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CouponExhaustedException extends RuntimeException {

    public CouponExhaustedException(String code) {
        super("Coupon with code '%s' has reached its usage limit".formatted(code));
    }
}

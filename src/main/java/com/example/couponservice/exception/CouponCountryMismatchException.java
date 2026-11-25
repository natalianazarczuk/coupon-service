package com.example.couponservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CouponCountryMismatchException extends RuntimeException {

    public CouponCountryMismatchException(String code) {
        super("Coupon with code '%s' is not available in this country".formatted(code));
    }
}
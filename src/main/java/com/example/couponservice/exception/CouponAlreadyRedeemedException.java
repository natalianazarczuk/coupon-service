package com.example.couponservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CouponAlreadyRedeemedException extends RuntimeException {

    public CouponAlreadyRedeemedException(String code) {
        super("Coupon with code '%s' has already been redeemed by this user".formatted(code));
    }
}

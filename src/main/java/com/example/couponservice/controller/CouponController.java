package com.example.couponservice.controller;

import com.example.couponservice.dto.CreateCouponRequest;
import com.example.couponservice.dto.CouponResponse;
import com.example.couponservice.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(
            @Valid
            @RequestBody
            CreateCouponRequest request
    ) {
        return couponService.createCoupon(request);
    }
}
package com.example.couponservice.controller;

import com.example.couponservice.client.GeolocationClient;
import com.example.couponservice.dto.CreateCouponRequest;
import com.example.couponservice.dto.CouponResponse;
import com.example.couponservice.dto.RedeemCouponRequest;
import com.example.couponservice.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final GeolocationClient geolocationClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(
            @Valid @RequestBody CreateCouponRequest request
    ) {
        return couponService.createCoupon(request);
    }

    @PostMapping("/redeem")
    public CouponResponse redeemCoupon(
            @Valid @RequestBody RedeemCouponRequest request,
            HttpServletRequest httpRequest
    ) {
        var ipAddress = httpRequest.getRemoteAddr();
        var userCountry = geolocationClient.getCountry(ipAddress);

        return couponService.redeemCoupon(request, userCountry);
    }

}
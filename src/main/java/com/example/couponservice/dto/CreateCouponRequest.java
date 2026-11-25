package com.example.couponservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCouponRequest(

        @NotBlank
        String code,

        @Positive
        int maxUsages,

        @NotBlank
        @Size(min = 2, max = 2)
        String country
){}
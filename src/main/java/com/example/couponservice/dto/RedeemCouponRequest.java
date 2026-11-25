package com.example.couponservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RedeemCouponRequest(
        @NotBlank
        @Size(max = 255)
        String code,

        @NotNull
        UUID userUuid
) {
}
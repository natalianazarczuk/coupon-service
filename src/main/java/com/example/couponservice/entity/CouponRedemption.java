package com.example.couponservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coupon_redemptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private UUID userUuid;

    @Column(nullable = false)
    private Instant redeemedAt;

    public CouponRedemption(Long couponId, UUID userUuid, Instant redeemedAt) {
        this.couponId = couponId;
        this.userUuid = userUuid;
        this.redeemedAt = redeemedAt;
    }
}
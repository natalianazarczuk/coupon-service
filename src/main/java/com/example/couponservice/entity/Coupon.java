package com.example.couponservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private int maxUsages;

    @Column(nullable = false)
    private int currentUsages;

    @Column(nullable = false, length = 2)
    private String country;

    public Coupon(String code, Instant createdAt, int maxUsages, String country) {
        this.code = code;
        this.createdAt = createdAt;
        this.maxUsages = maxUsages;
        this.currentUsages = 0;
        this.country = country;
    }
}
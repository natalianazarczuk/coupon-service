package com.example.couponservice.service;

import com.example.couponservice.dto.RedeemCouponRequest;
import com.example.couponservice.entity.Coupon;
import com.example.couponservice.exception.*;
import com.example.couponservice.repository.CouponRepository;
import com.example.couponservice.repository.CouponRedemptionRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("it")
class CouponServiceIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponRedemptionRepository couponRedemptionRepository;

    @AfterEach
    void cleanUp() {
        couponRedemptionRepository.deleteAllInBatch();
        couponRepository.deleteAllInBatch();
    }

    @Test
    void shouldRedeemCoupon() {
        // given
        var coupon = createCoupon("kupon123", 5);
        var userUuid = UUID.randomUUID();
        var request = new RedeemCouponRequest(coupon.getCode(), userUuid);

        // when
        var response = couponService.redeemCoupon(request, "PL");

        // then
        var updatedCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow();

        Assertions.assertThat(response.currentUsages()).isEqualTo(1);
        Assertions.assertThat(updatedCoupon.getCurrentUsages()).isEqualTo(1);

        Assertions.assertThat(
                couponRedemptionRepository.existsByCouponIdAndUserUuid(
                        coupon.getId(), userUuid)
        ).isTrue();
    }

    @Test
    void shouldRedeemCouponIgnoringCase() {
        // given
        var coupon = createCoupon("kuponWiosna", 5);
        var request = new RedeemCouponRequest("KUPONWIOSNA", UUID.randomUUID());

        // when
        couponService.redeemCoupon(request, "PL");

        // then
        var updatedCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow();

        Assertions.assertThat(updatedCoupon.getCurrentUsages()).isEqualTo(1);
    }

    @Test
    void shouldRejectRedemptionWhenCouponIsExhausted() {
        // given
        var coupon = createCoupon("wykorzystanykupon", 1);

        couponService.redeemCoupon(
                new RedeemCouponRequest(coupon.getCode(), UUID.randomUUID()),
                "PL"
        );

        var request = new RedeemCouponRequest(coupon.getCode(), UUID.randomUUID());

        // when, then
        Assertions.assertThatThrownBy(() ->
                couponService.redeemCoupon(request, "PL")
        ).isInstanceOf(CouponExhaustedException.class);

        var updatedCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow();

        Assertions.assertThat(updatedCoupon.getCurrentUsages()).isEqualTo(1);
        Assertions.assertThat(couponRedemptionRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectRedemptionWhenUserAlreadyRedeemedCoupon() {
        // given
        var coupon = createCoupon("powtorzonykupon", 5);
        var request = new RedeemCouponRequest(coupon.getCode(), UUID.randomUUID());

        couponService.redeemCoupon(request, "PL");

        // when, then
        Assertions.assertThatThrownBy(() ->
                couponService.redeemCoupon(request, "PL")
        ).isInstanceOf(CouponAlreadyRedeemedException.class);

        var updatedCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow();

        Assertions.assertThat(updatedCoupon.getCurrentUsages()).isEqualTo(1);
        Assertions.assertThat(couponRedemptionRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectRedemptionFromInvalidCountry() {
        // given
        var coupon = createCoupon("kuponPL", 5);
        var request = new RedeemCouponRequest(coupon.getCode(), UUID.randomUUID());

        // when, then
        Assertions.assertThatThrownBy(() ->
                couponService.redeemCoupon(request, "UA")
        ).isInstanceOf(CouponCountryMismatchException.class);

        var updatedCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow();

        Assertions.assertThat(updatedCoupon.getCurrentUsages()).isZero();
        Assertions.assertThat(couponRedemptionRepository.count()).isZero();
    }

    @Test
    void shouldAllowOnlyOneRedemptionWhenCouponHasOneUsageLeft() throws Exception {
        // given
        var coupon = createCoupon("ostatniskupon", 1);

        var firstRequest = new RedeemCouponRequest(
                coupon.getCode(), UUID.randomUUID()
        );
        var secondRequest = new RedeemCouponRequest(
                coupon.getCode(), UUID.randomUUID()
        );

        var startLatch = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {

            var firstResult = executor.submit(() -> {
                startLatch.await();
                return tryRedeemCoupon(firstRequest);
            });

            var secondResult = executor.submit(() -> {
                startLatch.await();
                return tryRedeemCoupon(secondRequest);
            });

            // when
            startLatch.countDown();

            var first = firstResult.get(10, TimeUnit.SECONDS);
            var second = secondResult.get(10, TimeUnit.SECONDS);

            // then
            Assertions.assertThat(first).isNotEqualTo(second);

            var updatedCoupon = couponRepository.findById(coupon.getId())
                    .orElseThrow();

            Assertions.assertThat(updatedCoupon.getCurrentUsages()).isEqualTo(1);
            Assertions.assertThat(couponRedemptionRepository.count()).isEqualTo(1);
        }
    }

    @Test
    void shouldAllowOnlyOneRedemptionPerUserWhenRequestsAreConcurrent() throws Exception {
        // given
        var coupon = createCoupon("kuponjednorazowy", 100);
        var request = new RedeemCouponRequest(coupon.getCode(), UUID.randomUUID());

        var startLatch = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {

            var firstResult = executor.submit(() -> {
                startLatch.await();
                return tryRedeemCoupon(request);
            });

            var secondResult = executor.submit(() -> {
                startLatch.await();
                return tryRedeemCoupon(request);
            });

            // when
            startLatch.countDown();

            var first = firstResult.get(10, TimeUnit.SECONDS);
            var second = secondResult.get(10, TimeUnit.SECONDS);

            // then
            Assertions.assertThat(first).isNotEqualTo(second);

            var updatedCoupon = couponRepository.findById(coupon.getId())
                    .orElseThrow();

            Assertions.assertThat(updatedCoupon.getCurrentUsages()).isEqualTo(1);
            Assertions.assertThat(couponRedemptionRepository.count()).isEqualTo(1);
        }
    }

    private Coupon createCoupon(String code, int maxUsages) {
        return couponRepository.saveAndFlush(
                new Coupon(
                        code,
                        Instant.parse("2026-01-01T12:00:00Z"),
                        maxUsages,
                        "PL"
                )
        );
    }

    private boolean tryRedeemCoupon(RedeemCouponRequest request) {
        try {
            couponService.redeemCoupon(request, "PL");
            return true;
        } catch (CouponExhaustedException | CouponAlreadyRedeemedException e) {
            return false;
        }
    }
}
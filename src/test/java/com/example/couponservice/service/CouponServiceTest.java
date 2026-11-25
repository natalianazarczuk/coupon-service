package com.example.couponservice.service;

import com.example.couponservice.dto.CreateCouponRequest;
import com.example.couponservice.dto.RedeemCouponRequest;
import com.example.couponservice.entity.Coupon;
import com.example.couponservice.entity.CouponRedemption;
import com.example.couponservice.exception.*;
import com.example.couponservice.repository.CouponRedemptionRepository;
import com.example.couponservice.repository.CouponRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponRedemptionRepository couponRedemptionRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void shouldCreateCoupon() {
        // given
        var request = new CreateCouponRequest("kupon123", 5, "PL");
        var coupon = createCoupon("kupon123", 5);

        when(couponRepository.saveAndFlush(any(Coupon.class)))
                .thenReturn(coupon);

        // when
        var response = couponService.createCoupon(request);

        // then
        Assertions.assertThat(response.code()).isEqualTo("kupon123");
        Assertions.assertThat(response.maxUsages()).isEqualTo(5);
        Assertions.assertThat(response.country()).isEqualTo("PL");

        verify(couponRepository).saveAndFlush(any(Coupon.class));
    }

    @Test
    void shouldRejectDuplicateCouponCode() {
        // given
        var request = new CreateCouponRequest("kupon123", 5, "PL");

        when(couponRepository.saveAndFlush(any(Coupon.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate code"));

        // when, then
        Assertions.assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(CouponAlreadyExistsExcpetion.class)
                .hasMessage("Coupon with code 'kupon123' already exists");
    }

    @Test
    void shouldRedeemCoupon() {
        // given
        var request = new RedeemCouponRequest("kupon456", UUID.randomUUID());
        var coupon = createCoupon("kupon456", 100);

        when(couponRepository.findByCodeForUpdate(request.code()))
                .thenReturn(Optional.of(coupon));

        when(couponRedemptionRepository.existsByCouponIdAndUserUuid(
                coupon.getId(), request.userUuid()))
                .thenReturn(false);

        // when
        var response = couponService.redeemCoupon(request, "PL");

        // then
        Assertions.assertThat(response.code()).isEqualTo("kupon456");
        Assertions.assertThat(response.currentUsages()).isEqualTo(1);
    }

    @Test
    void shouldRejectRedemptionWhenCouponNotFound() {
        // given
        var request = new RedeemCouponRequest("zlykupon", UUID.randomUUID());

        when(couponRepository.findByCodeForUpdate(request.code()))
                .thenReturn(Optional.empty());

        // when, then
        Assertions.assertThatThrownBy(() -> couponService.redeemCoupon(request, "PL"))
                .isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    void shouldRejectRedemptionFromInvalidCountry() {
        // given
        var request = new RedeemCouponRequest("kuponPL", UUID.randomUUID());
        var coupon = createCoupon("kuponPL", 5);

        when(couponRepository.findByCodeForUpdate(request.code()))
                .thenReturn(Optional.of(coupon));

        // when, then
        Assertions.assertThatThrownBy(() -> couponService.redeemCoupon(request, "UA"))
                .isInstanceOf(CouponCountryMismatchException.class);

        Assertions.assertThat(coupon.getCurrentUsages()).isZero();
    }

    @Test
    void shouldRejectRedemptionWhenCouponIsExhausted() {
        // given
        var request = new RedeemCouponRequest("wykorzystanykupon", UUID.randomUUID());
        var coupon = createCoupon("wykorzystanykupon", 1);
        coupon.incrementUsages();

        when(couponRepository.findByCodeForUpdate(request.code()))
                .thenReturn(Optional.of(coupon));

        when(couponRedemptionRepository.existsByCouponIdAndUserUuid(
                coupon.getId(), request.userUuid()))
                .thenReturn(false);

        // when, then
        Assertions.assertThatThrownBy(() -> couponService.redeemCoupon(request, "PL"))
                .isInstanceOf(CouponExhaustedException.class);

        Assertions.assertThat(coupon.getCurrentUsages()).isEqualTo(1);

        verify(couponRedemptionRepository, never())
                .save(any(CouponRedemption.class));
    }

    @Test
    void shouldRejectRedemptionWhenUserAlreadyRedeemedCoupon() {
        // given
        var request = new RedeemCouponRequest("powtorzonykupon", UUID.randomUUID());
        var coupon = createCoupon("powtorzonykupon", 100);
        coupon.incrementUsages();

        when(couponRepository.findByCodeForUpdate(request.code()))
                .thenReturn(Optional.of(coupon));

        when(couponRedemptionRepository.existsByCouponIdAndUserUuid(
                coupon.getId(), request.userUuid()))
                .thenReturn(true);

        // when, then
        Assertions.assertThatThrownBy(() -> couponService.redeemCoupon(request, "PL"))
                .isInstanceOf(CouponAlreadyRedeemedException.class);

        Assertions.assertThat(coupon.getCurrentUsages()).isEqualTo(1);
    }


    private Coupon createCoupon(String code, int maxUsages) {
        var coupon = new Coupon(
                code,
                Instant.parse("2026-01-01T12:00:00Z"),
                maxUsages,
                "PL"
        );

        ReflectionTestUtils.setField(coupon, "id", 1L);

        return coupon;
    }

}
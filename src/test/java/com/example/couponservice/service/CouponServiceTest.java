package com.example.couponservice.service;

import com.example.couponservice.dto.CreateCouponRequest;
import com.example.couponservice.entity.Coupon;
import com.example.couponservice.exception.CouponAlreadyExistsExcpetion;
import com.example.couponservice.repository.CouponRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void shouldCreateCoupon() {
        // given
        var request = new CreateCouponRequest("kupon123", 5, "PL");

        var coupon = new Coupon(
                "kupon123",
                Instant.now(),
                5,
                "PL"
        );

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

}
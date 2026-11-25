package com.example.couponservice.repository;

import com.example.couponservice.entity.Coupon;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

@DataJpaTest
@ActiveProfiles("it")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CouponRepositoryIntegrationTest {

    @Autowired
    private CouponRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveCoupon() {
        // given
        var coupon = createCoupon();

        // when
        var savedCoupon = repository.saveAndFlush(coupon);

        // then
        Assertions.assertThat(savedCoupon.getId()).isNotNull();
        Assertions.assertThat(savedCoupon.getCurrentUsages()).isZero();
    }

    @Test
    void shouldFindCouponById() {
        // given
        var coupon = repository.saveAndFlush(createCoupon());
        entityManager.clear();

        // when
        var foundCoupon = repository.findById(coupon.getId())
                .orElseThrow();

        // then
        Assertions.assertThat(foundCoupon)
                .usingRecursiveComparison()
                .isEqualTo(coupon);
    }

    @Test
    void shouldReturnEmptyWhenCouponDoesNotExist() {
        // when
        var result = repository.findById(-1L);

        // then
        Assertions.assertThat(result).isEmpty();
    }

    private Coupon createCoupon() {
        return new Coupon(
                "niepodleglosc",
                Instant.parse("2026-11-11T12:00:00Z"),
                100,
                "PL"
        );
    }
}
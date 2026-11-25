package com.example.couponservice.repository;

import com.example.couponservice.entity.Coupon;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
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
        var coupon = createCoupon("kupon987");

        // when
        var savedCoupon = repository.saveAndFlush(coupon);

        // then
        Assertions.assertThat(savedCoupon.getId()).isNotNull();
        Assertions.assertThat(savedCoupon.getCurrentUsages()).isZero();
    }

    @Test
    void shouldFindCouponById() {
        // given
        var coupon = repository.saveAndFlush(createCoupon("kupon987"));
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

    @Test
    void shouldRejectDuplicateCouponCodeIgnoringCase() {
        // given
        repository.saveAndFlush(createCoupon("kupon456"));

        // when, then
        Assertions.assertThatThrownBy(() ->
                repository.saveAndFlush(createCoupon("KUPON456"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldSaveCouponsWithDifferentCodes() {
        // when
        var firstCoupon = repository.saveAndFlush(createCoupon("kupon123"));
        var secondCoupon = repository.saveAndFlush(createCoupon("123kupon"));

        // then
        Assertions.assertThat(firstCoupon.getId()).isNotNull();
        Assertions.assertThat(secondCoupon.getId()).isNotNull();
        Assertions.assertThat(firstCoupon.getId()).isNotEqualTo(secondCoupon.getId());
    }

    private Coupon createCoupon(String code) {
        return new Coupon(
                code ,
                Instant.parse("2026-01-01T12:00:00Z"),
                100,
                "PL"
        );
    }
}
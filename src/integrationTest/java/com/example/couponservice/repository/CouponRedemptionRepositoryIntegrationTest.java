package com.example.couponservice.repository;

import com.example.couponservice.entity.Coupon;
import com.example.couponservice.entity.CouponRedemption;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

@DataJpaTest
@ActiveProfiles("it")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CouponRedemptionRepositoryIntegrationTest {

    @Autowired
    private CouponRedemptionRepository repository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveCouponRedemption() {
        // given
        var coupon = createCoupon("kupon123");
        var userUuid = UUID.randomUUID();
        var redemption = createRedemption(coupon.getId(), userUuid);

        // when
        var savedRedemption = repository.saveAndFlush(redemption);

        // then
        Assertions.assertThat(savedRedemption.getId()).isNotNull();
        Assertions.assertThat(savedRedemption.getCouponId()).isEqualTo(coupon.getId());
        Assertions.assertThat(savedRedemption.getUserUuid()).isEqualTo(userUuid);
    }

    @Test
    void shouldFindCouponRedemptionById() {
        // given
        var coupon = createCoupon("kupon123");
        var redemption = repository.saveAndFlush(
                createRedemption(coupon.getId(), UUID.randomUUID())
        );
        entityManager.clear();

        // when
        var foundRedemption = repository.findById(redemption.getId())
                .orElseThrow();

        // then
        Assertions.assertThat(foundRedemption)
                .usingRecursiveComparison()
                .isEqualTo(redemption);
    }

    @Test
    void shouldRejectDuplicateCouponRedemption() {
        // given
        var coupon = createCoupon("kupon123");
        var userUuid = UUID.randomUUID();

        repository.saveAndFlush(
                createRedemption(coupon.getId(), userUuid)
        );

        // when, then
        Assertions.assertThatThrownBy(() ->
                repository.saveAndFlush(
                        createRedemption(coupon.getId(), userUuid)
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowDifferentUsersToRedeemSameCoupon() {
        // given
        var coupon = createCoupon("kupon123");

        // when
        var firstRedemption = repository.saveAndFlush(
                createRedemption(coupon.getId(), UUID.randomUUID())
        );
        var secondRedemption = repository.saveAndFlush(
                createRedemption(coupon.getId(), UUID.randomUUID())
        );

        // then
        Assertions.assertThat(firstRedemption.getId()).isNotNull();
        Assertions.assertThat(secondRedemption.getId()).isNotNull();
        Assertions.assertThat(firstRedemption.getId())
                .isNotEqualTo(secondRedemption.getId());
    }

    @Test
    void shouldAllowSameUserToRedeemDifferentCoupons() {
        // given
        var firstCoupon = createCoupon("kupon123");
        var secondCoupon = createCoupon("kupon456");
        var userUuid = UUID.randomUUID();

        // when
        var firstRedemption = repository.saveAndFlush(
                createRedemption(firstCoupon.getId(), userUuid)
        );
        var secondRedemption = repository.saveAndFlush(
                createRedemption(secondCoupon.getId(), userUuid)
        );

        // then
        Assertions.assertThat(firstRedemption.getId()).isNotNull();
        Assertions.assertThat(secondRedemption.getId()).isNotNull();
        Assertions.assertThat(firstRedemption.getId())
                .isNotEqualTo(secondRedemption.getId());
    }

    @Test
    void shouldReturnTrueWhenUserAlreadyRedeemedCoupon() {
        // given
        var coupon = createCoupon("kupon123");
        var userUuid = UUID.randomUUID();

        repository.saveAndFlush(
                createRedemption(coupon.getId(), userUuid)
        );

        // when
        var result = repository.existsByCouponIdAndUserUuid(
                coupon.getId(), userUuid
        );

        // then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserHasNotRedeemedCoupon() {
        // given
        var coupon = createCoupon("kupon123");

        repository.saveAndFlush(
                createRedemption(coupon.getId(), UUID.randomUUID())
        );

        // when
        var result = repository.existsByCouponIdAndUserUuid(
                coupon.getId(), UUID.randomUUID()
        );

        // then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void shouldFailRedemptionIfCouponNotExists() {
        // given
        var redemption = createRedemption(-1L, UUID.randomUUID());

        // when, then
        Assertions.assertThatThrownBy(() ->
                repository.saveAndFlush(redemption)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Coupon createCoupon(String code) {
        return couponRepository.saveAndFlush(
                new Coupon(
                        code,
                        Instant.parse("2026-01-01T12:00:00Z"),
                        100,
                        "PL"
                )
        );
    }

    private CouponRedemption createRedemption(Long couponId, UUID userUuid) {
        return new CouponRedemption(
                couponId,
                userUuid,
                Instant.parse("2026-01-02T12:00:00Z")
        );
    }
}
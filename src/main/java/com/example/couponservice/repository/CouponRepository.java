package com.example.couponservice.repository;

import com.example.couponservice.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c FROM Coupon c
            WHERE LOWER(c.code) = LOWER(:code)
            """)
    Optional<Coupon> findByCodeForUpdate(@Param("code") String code);

}
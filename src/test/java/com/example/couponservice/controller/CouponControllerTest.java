package com.example.couponservice.controller;

import com.example.couponservice.client.GeolocationClient;
import com.example.couponservice.dto.CreateCouponRequest;
import com.example.couponservice.dto.CouponResponse;
import com.example.couponservice.dto.RedeemCouponRequest;
import com.example.couponservice.exception.*;
import com.example.couponservice.service.CouponService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponService couponService;

    @MockitoBean
    private GeolocationClient geolocationClient;

    @Test
    void shouldCreateCoupon() throws Exception {
        // given
        var request = new CreateCouponRequest("kupon123", 5, "PL");

        var response = new CouponResponse(
                1L,
                "kupon123",
                Instant.now(),
                5,
                0,
                "PL"
        );

        when(couponService.createCoupon(request)).thenReturn(response);

        // when, then
        mockMvc.perform(MockMvcRequestBuilders.post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "kupon123",
                                    "maxUsages": 5,
                                    "country": "PL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("kupon123"))
                .andExpect(jsonPath("$.country").value("PL"));

        verify(couponService).createCoupon(request);
    }

    @Test
    void shouldRejectBlankCouponCode() throws Exception {
        // when, then
        mockMvc.perform(MockMvcRequestBuilders.post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "",
                                    "maxUsages": 100,
                                    "country": "PL"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(couponService);
    }

    @Test
    void shouldRejectInvalidMaxUsages() throws Exception {
        // when, then
        mockMvc.perform(MockMvcRequestBuilders.post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "kupon234",
                                    "maxUsages": -10,
                                    "country": "PL"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(couponService);
    }

    @Test
    void shouldRejectInvalidCountryLength() throws Exception {
        // when, then
        mockMvc.perform(MockMvcRequestBuilders.post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "kupon345",
                                    "maxUsages": 7,
                                    "country": "POL"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(couponService);
    }

    @Test
    void shouldRedeemCoupon() throws Exception {
        // given
        var userUuid = UUID.randomUUID();
        var request = new RedeemCouponRequest("kupon456", userUuid);

        var response = new CouponResponse(
                1L,
                "kupon456",
                Instant.parse("2026-01-01T12:00:00Z"),
                5,
                1,
                "PL"
        );

        when(geolocationClient.getCountry("8.8.8.8"))
                .thenReturn("PL");

        when(couponService.redeemCoupon(request, "PL"))
                .thenReturn(response);

        // when, then
        mockMvc.perform(MockMvcRequestBuilders.post("/coupons/redeem")
                        .with(httpRequest -> {
                            httpRequest.setRemoteAddr("8.8.8.8");
                            return httpRequest;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "code": "kupon456",
                                "userUuid": "%s"
                            }
                            """.formatted(userUuid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("kupon456"))
                .andExpect(jsonPath("$.currentUsages").value(1))
                .andExpect(jsonPath("$.country").value("PL"));

        verify(geolocationClient).getCountry("8.8.8.8");
        verify(couponService).redeemCoupon(request, "PL");
    }

    @Test
    void shouldRejectDuplicateCouponCode() throws Exception {
        // given
        var request = new CreateCouponRequest("powtorzonykupon", 5, "PL");

        when(couponService.createCoupon(request))
                .thenThrow(new CouponAlreadyExistsException(
                        request.code(),
                        new DataIntegrityViolationException("Duplicate code")
                ));

        // when, then
        mockMvc.perform(MockMvcRequestBuilders.post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "code": "powtorzonykupon",
                                "maxUsages": 5,
                                "country": "PL"
                            }
                            """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectRedemptionWhenGeolocationIsUnavailable() throws Exception {
        // given
        when(geolocationClient.getCountry(anyString()))
                .thenThrow(new GeolocationUnavailableException());

        // when, then
        mockMvc.perform(MockMvcRequestBuilders.post("/coupons/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "code": "kupon789",
                                "userUuid": "%s"
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isServiceUnavailable());

        verifyNoInteractions(couponService);
    }

    @ParameterizedTest
    @MethodSource("generateRedeemErrors")
    void shouldRejectInvalidCouponRedemption(
            String code,
            RuntimeException exception,
            int expectedStatus
    ) throws Exception {
        // given
        var userUuid = UUID.randomUUID();
        var request = new RedeemCouponRequest(code, userUuid);

        when(geolocationClient.getCountry(anyString()))
                .thenReturn("PL");

        when(couponService.redeemCoupon(request, "PL"))
                .thenThrow(exception);

        // when, then
        mockMvc.perform(MockMvcRequestBuilders.post("/coupons/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "code": "%s",
                                "userUuid": "%s"
                            }
                            """.formatted(code, userUuid)))
                .andExpect(status().is(expectedStatus));
    }

    private static Stream<Arguments> generateRedeemErrors() {
        return Stream.of(
                Arguments.of(
                        "zlykupon",
                        new CouponNotFoundException("zlykupon"),
                        404
                ),
                Arguments.of(
                        "wykorzystanykupon",
                        new CouponExhaustedException("wykorzystanykupon"),
                        409
                ),
                Arguments.of(
                        "powtorzonykupon",
                        new CouponAlreadyRedeemedException("powtorzonykupon"),
                        409
                ),
                Arguments.of(
                        "kuponzagraniczny",
                        new CouponCountryMismatchException("kuponzagraniczny"),
                        403
                )
        );
    }
}
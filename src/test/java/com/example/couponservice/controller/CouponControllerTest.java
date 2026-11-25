package com.example.couponservice.controller;

import com.example.couponservice.dto.CreateCouponRequest;
import com.example.couponservice.dto.CouponResponse;
import com.example.couponservice.service.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponService couponService;

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
}
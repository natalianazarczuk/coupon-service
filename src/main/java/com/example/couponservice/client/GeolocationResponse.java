package com.example.couponservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeolocationResponse(
        String countryCode
) {
}

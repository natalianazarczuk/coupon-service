package com.example.couponservice.client;

import com.example.couponservice.exception.GeolocationUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class GeolocationClient {

    private final RestClient geolocationRestClient;

    public String getCountry(String ipAddress) {
        try {
            var response = geolocationRestClient.get()
                    .uri("/api/v1/json/{ip}", ipAddress)
                    .retrieve()
                    .body(GeolocationResponse.class);

            if (response == null || response.countryCode() == null || !response.countryCode().matches("[A-Z]{2}")) {
                throw new GeolocationUnavailableException();
            }

            return response.countryCode();

        } catch (RestClientException e) {
            throw new GeolocationUnavailableException(e);
        }
    }
}
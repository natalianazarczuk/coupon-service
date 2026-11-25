package com.example.couponservice.client;

import com.example.couponservice.exception.GeolocationUnavailableException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GeolocationClientTest {

    private static final String BASE_URL = "https://free.freeipapi.com";

    private MockRestServiceServer server;

    private GeolocationClient geolocationClient;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder()
                .baseUrl(BASE_URL);

        server = MockRestServiceServer.bindTo(builder).build();
        geolocationClient = new GeolocationClient(builder.build());
    }

    @Test
    void shouldReturnCountryForIpAddress() {
        // given
        var ipAddress = "8.8.8.8";

        server.expect(requestTo(BASE_URL + "/api/v1/json/" + ipAddress))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                            "ipAddress": "8.8.8.8",
                            "countryName": "United States",
                            "countryCode": "US"
                        }
                        """, MediaType.APPLICATION_JSON));

        // when
        var country = geolocationClient.getCountry(ipAddress);

        // then
        Assertions.assertThat(country).isEqualTo("US");

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenCountryCodeIsMissing() {
        // given
        var ipAddress = "1.1.1.1";

        server.expect(requestTo(BASE_URL + "/api/v1/json/" + ipAddress))
                .andRespond(withSuccess("""
                        {
                            "ipAddress": "1.1.1.1"
                        }
                        """, MediaType.APPLICATION_JSON));

        // when, then
        Assertions.assertThatThrownBy(() ->
                geolocationClient.getCountry(ipAddress)
        ).isInstanceOf(GeolocationUnavailableException.class);

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenCountryCodeIsInvalid() {
        // given
        var ipAddress = "9.9.9.9";

        server.expect(requestTo(BASE_URL + "/api/v1/json/" + ipAddress))
                .andRespond(withSuccess("""
                        {
                            "countryCode": "USA"
                        }
                        """, MediaType.APPLICATION_JSON));

        // when, then
        Assertions.assertThatThrownBy(() ->
                geolocationClient.getCountry(ipAddress)
        ).isInstanceOf(GeolocationUnavailableException.class);

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenResponseIsEmpty() {
        // given
        var ipAddress = "208.67.222.222";

        server.expect(requestTo(BASE_URL + "/api/v1/json/" + ipAddress))
                .andRespond(withSuccess());

        // when, then
        Assertions.assertThatThrownBy(() ->
                geolocationClient.getCountry(ipAddress)
        ).isInstanceOf(GeolocationUnavailableException.class);

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenApiIsUnavailable() {
        // given
        var ipAddress = "8.8.4.4";

        server.expect(requestTo(BASE_URL + "/api/v1/json/" + ipAddress))
                .andRespond(withServerError());

        // when, then
        Assertions.assertThatThrownBy(() ->
                geolocationClient.getCountry(ipAddress)
        ).isInstanceOf(GeolocationUnavailableException.class);

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenRateLimitIsExceeded() {
        // given
        var ipAddress = "1.0.0.1";

        server.expect(requestTo(BASE_URL + "/api/v1/json/" + ipAddress))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        // when, then
        Assertions.assertThatThrownBy(() ->
                geolocationClient.getCountry(ipAddress)
        ).isInstanceOf(GeolocationUnavailableException.class);

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenRequestTimesOut() {
        // given
        var ipAddress = "149.112.112.112";

        server.expect(requestTo(BASE_URL + "/api/v1/json/" + ipAddress))
                .andRespond(withException(
                        new SocketTimeoutException("Read timed out")
                ));

        // when, then
        Assertions.assertThatThrownBy(() ->
                geolocationClient.getCountry(ipAddress)
        ).isInstanceOf(GeolocationUnavailableException.class);

        server.verify();
    }
}
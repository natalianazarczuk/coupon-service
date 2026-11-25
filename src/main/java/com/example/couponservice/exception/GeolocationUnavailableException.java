package com.example.couponservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class GeolocationUnavailableException extends RuntimeException {

    public GeolocationUnavailableException() {
        super("Nie udało się ustalić kraju użytka");
    }

    public GeolocationUnavailableException(Throwable cause) {
        super("Nie udało się ustalić kraju użytka", cause);
    }
}
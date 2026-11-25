package com.example.couponservice.exception;

public class GeolocationUnavailableException extends RuntimeException {

    public GeolocationUnavailableException() {
        super("Nie udało się ustalić kraju użytka");
    }

    public GeolocationUnavailableException(Throwable cause) {
        super("Nie udało się ustalić kraju użytka", cause);
    }
}
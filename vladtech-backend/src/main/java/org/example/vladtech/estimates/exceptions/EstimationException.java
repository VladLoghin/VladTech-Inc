package org.example.vladtech.estimates.exceptions;

public class EstimationException extends RuntimeException {
    private final String code;

    public EstimationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
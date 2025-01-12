package io.shinmen.chronos.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidJwtAuthenticationException extends ApiException {
    public InvalidJwtAuthenticationException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}

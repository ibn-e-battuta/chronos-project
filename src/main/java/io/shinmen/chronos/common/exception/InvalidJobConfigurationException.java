package io.shinmen.chronos.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidJobConfigurationException extends ApiException {
    public InvalidJobConfigurationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

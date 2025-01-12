package io.shinmen.chronos.common.exception;

import org.springframework.http.HttpStatus;

public class JobValidationException extends ApiException {
    public JobValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

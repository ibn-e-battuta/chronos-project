package io.shinmen.chronos.common.exception;

import org.springframework.http.HttpStatus;

public class JobNotFoundException extends ApiException {
    public JobNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

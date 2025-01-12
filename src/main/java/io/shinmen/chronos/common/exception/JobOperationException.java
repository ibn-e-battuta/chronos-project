package io.shinmen.chronos.common.exception;

import org.springframework.http.HttpStatus;

public class JobOperationException extends ApiException {
    public JobOperationException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}

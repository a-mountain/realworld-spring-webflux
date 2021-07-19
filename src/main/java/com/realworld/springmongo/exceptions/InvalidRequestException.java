package com.realworld.springmongo.exceptions;

import lombok.Getter;

public class InvalidRequestException extends RuntimeException {
    @Getter
    private final String subject;
    @Getter
    private final String violation;

    public InvalidRequestException(String subject, String violation) {
        super(subject + ": " + violation);
        this.subject = subject;
        this.violation = violation;
    }

    public InvalidRequestException(String subject, String violation, Throwable cause) {
        super(subject + ": " + violation, cause);
        this.subject = subject;
        this.violation = violation;
    }
}

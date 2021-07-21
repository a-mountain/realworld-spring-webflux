package com.realworld.springmongo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public InvalidRequestExceptionResponse invalidRequestExceptionHandler(InvalidRequestException e) {
        var subject = e.getSubject();
        var violation = e.getViolation();
        var errors = Map.of(subject, List.of(violation));
        return new InvalidRequestExceptionResponse(errors);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public InvalidRequestExceptionResponse handleConstraintViolationException(WebExchangeBindException ex) {
        Map<String, List<String>> body = new HashMap<>();
        for (FieldError fieldError : ex.getFieldErrors()) {
            body.putIfAbsent(fieldError.getField(), new ArrayList<>());
            var errors = body.get(fieldError.getField());
            errors.add(fieldError.getDefaultMessage());
        }
        return new InvalidRequestExceptionResponse(body);
    }

    record InvalidRequestExceptionResponse(Map<String, List<String>> errors) {
    }
}

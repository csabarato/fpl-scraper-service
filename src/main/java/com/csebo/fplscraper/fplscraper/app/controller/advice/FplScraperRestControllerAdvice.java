package com.csebo.fplscraper.fplscraper.app.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FplScraperRestControllerAdvice {

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(Exception exception) {
        return new ResponseEntity<>("Error: "+ exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(Exception exception) {
        return new ResponseEntity<>("Error: "+ exception, HttpStatus.BAD_REQUEST);
    }
}

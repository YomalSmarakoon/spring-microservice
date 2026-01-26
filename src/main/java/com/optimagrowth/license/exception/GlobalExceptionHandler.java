package com.optimagrowth.license.exception;

import com.optimagrowth.license.model.dto.CircuitBreakerErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*@ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ApiErrorResponse> handleCircuitOpen(CallNotPermittedException ex) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE,
                "License service circuit breaker is OPEN");
    }*/

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<CircuitBreakerErrorResponse> handleCircuitOpen(CallNotPermittedException ex) {

        // Screenshot shows: NOT_ACCEPTABLE + message from exception
        HttpStatus status = HttpStatus.NOT_ACCEPTABLE;

        var body = new CircuitBreakerErrorResponse(
                new CircuitBreakerErrorResponse.Metadata(status.name()),
                List.of(new CircuitBreakerErrorResponse.ApiErrorResponse(
                        ex.getMessage(),
                        null,
                        ex.getMessage()
                ))
        );

        return ResponseEntity.status(status).body(body);
    }
}

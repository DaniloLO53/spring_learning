package org.example.project.exceptions;

import org.example.project.payload.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> customAPIException(APIException e) {
        String message = e.getMessage();
        APIResponse response = new APIResponse(message);

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}

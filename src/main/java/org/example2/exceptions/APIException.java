package org.example2.exceptions;

public class APIException extends RuntimeException {
    public APIException(String message) {
        super(message);
    }
}

package com.example.book.gateway.Exception;

public class BadRequestException extends RuntimeException{
    String message;

    public BadRequestException(String message) {
        super(message);
    }
}


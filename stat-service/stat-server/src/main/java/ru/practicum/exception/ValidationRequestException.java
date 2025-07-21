package ru.practicum.exception;

public class ValidationRequestException extends RuntimeException {

    public ValidationRequestException(String message) {
        super(message);
    }
}
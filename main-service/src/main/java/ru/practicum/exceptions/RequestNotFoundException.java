package ru.practicum.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class RequestNotFoundException extends EntityNotFoundException {

    public RequestNotFoundException(Long id) {
        super(String.format("Запрос с id=%d не найден.", id));
    }
}
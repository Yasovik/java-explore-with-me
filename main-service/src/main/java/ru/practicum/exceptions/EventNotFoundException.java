package ru.practicum.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class EventNotFoundException extends EntityNotFoundException {

    public EventNotFoundException(long id) {
        super(String.format("Событие с id=%d не найдено.", id));
    }
}
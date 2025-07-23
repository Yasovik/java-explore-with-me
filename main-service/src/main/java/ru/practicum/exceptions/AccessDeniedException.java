package ru.practicum.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class AccessDeniedException extends EntityNotFoundException {
    public AccessDeniedException(Long id) {
        super(String.format("Пользователь с id=%d не является автором комментария.", id));
    }
}

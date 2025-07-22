package ru.practicum.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class CommentNotFoundException extends EntityNotFoundException {
    public CommentNotFoundException(Long id) {
        super(String.format("Комментарий с id=%d не найден.", id));
    }
}

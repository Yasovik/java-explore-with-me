package ru.practicum.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class CategoryNotFoundException extends EntityNotFoundException {

    public CategoryNotFoundException(long id) {
        super(String.format("Категория с id=%d не найдена.", id));
    }
}
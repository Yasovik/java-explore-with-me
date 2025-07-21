package ru.practicum.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class CompilationNotFoundException extends EntityNotFoundException {

    public CompilationNotFoundException(Long id) {
        super(String.format("Сборка с id=%d не найдена.", id));
    }
}
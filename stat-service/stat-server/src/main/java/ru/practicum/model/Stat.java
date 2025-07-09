package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Stat {
    private final String app;
    private final String uri;
    private final Long hits;
}
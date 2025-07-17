package ru.practicum.mapper;

import ru.practicum.StatDto;
import ru.practicum.model.Stat;

public class StatMapper {
    private StatMapper() {

    }

    public static StatDto toStatDto(Stat stat) {
        return StatDto.builder()
                .app(stat.getApp())
                .uri(stat.getUri())
                .hits(stat.getHits())
                .build();
    }
}
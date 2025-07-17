package ru.practicum.location.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.location.model.Location;

@NoArgsConstructor
public class LocationMapper {

    public static LocationDto toLocationDto(Location location) {
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    public static Location toLocation(LocationDto locationDto) {
        return Location.builder()
                .lat(locationDto.getLat())
                .lon(locationDto.getLon())
                .build();
    }
}
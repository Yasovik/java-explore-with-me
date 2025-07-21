package ru.practicum.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.location.dto.LocationDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank(message = "Заголовок не должен быть пустым")
    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3 до 120 символов")
    private String title;

    @NotBlank(message = "Аннотация не должна быть пустой")
    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов")
    private String annotation;

    @PositiveOrZero(message = "ID категории должен быть нулевым или положительным")
    private long category;

    @NotBlank(message = "Описание не должно быть пустым")
    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов")
    private String description;

    @NotNull(message = "Дата события не должна быть null")
    private String eventDate;

    @NotNull(message = "Локация не должна быть null")
    private LocationDto location;

    @Builder.Default
    private boolean paid = false;

    @Builder.Default
    private int participantLimit = 0;

    @Builder.Default
    private boolean requestModeration = true;
}
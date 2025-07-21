package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequestDto {

    @NotBlank(message = "Имя не должно быть пустым.")
    @Size(min = 2, max = 250, message = "Имя должно содержать от 2 до 250 символов.")
    private String name;

    @NotBlank(message = "Email не должен быть пустым.")
    @Email(message = "Email должен быть корректным.")
    @Size(min = 6, max = 254, message = "Email должен содержать от 6 до 254 символов.")
    private String email;
}
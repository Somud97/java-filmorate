package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * User.
 */
@Getter
@Setter
public class User {
    private int id;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать символ @ и быть корректной")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    @NotBlank(message = "Имя не должно быть пустым")
    private String name;

    @NotNull(message = "Дата рождения не может быть пустой")
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}

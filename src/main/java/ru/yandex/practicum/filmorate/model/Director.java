package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Director {
    private int id;

    @NotBlank(message = "Имя режиссёра не должно быть пустым")
    private String name;
}

package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.dto.DirectorDto;

@Setter
@Getter
public class Director extends DirectorDto {
    private int id;

    @NotBlank(message = "Имя режиссёра не должно быть пустым")
    private String name;
}

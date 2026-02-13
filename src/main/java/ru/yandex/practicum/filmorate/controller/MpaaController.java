package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.dto.MpaaDto;
import ru.yandex.practicum.filmorate.storage.mpaa.MpaaStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Validated
public class MpaaController {

    private final MpaaStorage mpaaStorage;

    public MpaaController(MpaaStorage mpaaStorage) {
        this.mpaaStorage = mpaaStorage;
    }

    @GetMapping
    public List<MpaaDto> getAllMpaa() {
        return mpaaStorage.findAll();
    }

    @GetMapping("/{id}")
    public MpaaDto getMpaaById(@PathVariable @Positive int id) {
        return mpaaStorage.findById(id);
    }
}

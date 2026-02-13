package ru.yandex.practicum.filmorate.storage.mpaa;

import ru.yandex.practicum.filmorate.model.dto.MpaaDto;

import java.util.List;

public interface MpaaStorage {

    List<MpaaDto> findAll();

    MpaaDto findById(int id);
}

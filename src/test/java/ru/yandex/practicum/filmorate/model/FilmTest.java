package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {

    @Test
    void constructorDefaultShouldCreateEmptyFilm() {
        Film film = new Film();

        assertEquals(0, film.getId());
        assertNull(film.getName());
        assertNull(film.getDescription());
        assertNull(film.getReleaseDate());
        assertEquals(0, film.getDuration());
    }

    @Test
    void setIdValidIdShouldSetId() {
        Film film = new Film();
        int id = 123;

        film.setId(id);

        assertEquals(id, film.getId());
    }

    @Test
    void setNameValidNameShouldSetName() {
        Film film = new Film();
        String name = "Test Film";

        film.setName(name);

        assertEquals(name, film.getName());
    }

    @Test
    void setNameNullNameShouldSetNullName() {
        Film film = new Film();

        film.setName(null);

        assertNull(film.getName());
    }

    @Test
    void setDescriptionValidDescriptionShouldSetDescription() {
        Film film = new Film();
        String description = "Test Description";

        film.setDescription(description);

        assertEquals(description, film.getDescription());
    }

    @Test
    void setDescriptionNullDescriptionShouldSetNullDescription() {
        Film film = new Film();

        film.setDescription(null);

        assertNull(film.getDescription());
    }

    @Test
    void setReleaseDateValidDateShouldSetReleaseDate() {
        Film film = new Film();
        LocalDate releaseDate = LocalDate.of(2000, 1, 1);

        film.setReleaseDate(releaseDate);

        assertEquals(releaseDate, film.getReleaseDate());
    }

    @Test
    void setReleaseDateNullDateShouldSetNullReleaseDate() {
        Film film = new Film();

        film.setReleaseDate(null);

        assertNull(film.getReleaseDate());
    }

    @Test
    void setDurationValidDurationShouldSetDuration() {
        Film film = new Film();
        int duration = 120;

        film.setDuration(duration);

        assertEquals(duration, film.getDuration());
    }

    @Test
    void setDurationZeroDurationShouldSetZeroDuration() {
        Film film = new Film();

        film.setDuration(0);

        assertEquals(0, film.getDuration());
    }

    @Test
    void setDurationNegativeDurationShouldSetNegativeDuration() {
        Film film = new Film();

        film.setDuration(-10);

        assertEquals(-10, film.getDuration());
    }
}
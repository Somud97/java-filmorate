package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.validation.ValidationUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class, EventDbStorage.class, ValidationUtils.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void add_shouldCreateFilmAndReturnWithId() {
        Film film = createFilm("Film One", "Description", LocalDate.of(2000, 6, 15), 120);

        Film created = filmStorage.add(film);

        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("Film One");
        assertThat(created.getDescription()).isEqualTo("Description");
        assertThat(created.getReleaseDate()).isEqualTo(LocalDate.of(2000, 6, 15));
        assertThat(created.getDuration()).isEqualTo(120);
    }

    @Test
    void add_shouldSaveMpaaRating() {
        Film film = createFilm("Rated", "Desc", LocalDate.of(2010, 1, 1), 90);
        film.setMpaaRating(MpaaRating.PG_13);

        Film created = filmStorage.add(film);

        assertThat(created.getMpaaRating()).isEqualTo(MpaaRating.PG_13);
        Film found = filmStorage.findById(created.getId());
        assertThat(found.getMpaaRating()).isEqualTo(MpaaRating.PG_13);
    }

    @Test
    void add_shouldSaveLikes() {
        User u1 = new User();
        u1.setEmail("u1@mail.ru");
        u1.setLogin("u1");
        u1.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.add(u1);
        User u2 = new User();
        u2.setEmail("u2@mail.ru");
        u2.setLogin("u2");
        u2.setBirthday(LocalDate.of(1991, 1, 1));
        userStorage.add(u2);

        Film film = createFilm("Liked", "Desc", LocalDate.of(2020, 1, 1), 80);
        Set<Integer> likes = new HashSet<>();
        likes.add(u1.getId());
        likes.add(u2.getId());
        film.setLikes(likes);

        Film created = filmStorage.add(film);

        assertThat(created.getLikes()).containsExactlyInAnyOrder(u1.getId(), u2.getId());
        Film found = filmStorage.findById(created.getId());
        assertThat(found.getLikes()).containsExactlyInAnyOrder(u1.getId(), u2.getId());
    }

    @Test
    void update_shouldUpdateFilmAndReturnUpdated() {
        Film film = filmStorage.add(createFilm("Old", "Old desc", LocalDate.of(1999, 1, 1), 90));
        film.setName("Updated");
        film.setDescription("Updated desc");
        film.setDuration(95);

        Film updated = filmStorage.update(film);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getDescription()).isEqualTo("Updated desc");
        assertThat(updated.getDuration()).isEqualTo(95);
        Film found = filmStorage.findById(film.getId());
        assertThat(found.getName()).isEqualTo("Updated");
    }

    @Test
    void update_shouldThrowWhenFilmNotFound() {
        Film film = new Film();
        film.setId(99999);
        film.setName("X");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(60);

        assertThatThrownBy(() -> filmStorage.update(film))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    void delete_shouldRemoveFilm() {
        Film film = filmStorage.add(createFilm("ToDelete", "Desc", LocalDate.of(2005, 1, 1), 70));
        int id = film.getId();

        filmStorage.deleteById(id);

        assertThatThrownBy(() -> filmStorage.findById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(String.valueOf(id));
    }

    @Test
    void delete_shouldThrowWhenFilmNotFound() {
        assertThatThrownBy(() -> filmStorage.deleteById(99999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    void findById_shouldReturnFilmWhenExists() {
        Film film = filmStorage.add(createFilm("Find Me", "Description", LocalDate.of(2012, 8, 20), 110));

        Film found = filmStorage.findById(film.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(film.getId());
        assertThat(found.getName()).isEqualTo("Find Me");
        assertThat(found.getDescription()).isEqualTo("Description");
        assertThat(found.getReleaseDate()).isEqualTo(LocalDate.of(2012, 8, 20));
        assertThat(found.getDuration()).isEqualTo(110);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> filmStorage.findById(99999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoFilms() {
        List<Film> all = filmStorage.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllFilms() {
        filmStorage.add(createFilm("Film A", "Desc A", LocalDate.of(2000, 1, 1), 90));
        filmStorage.add(createFilm("Film B", "Desc B", LocalDate.of(2001, 1, 1), 100));

        List<Film> all = filmStorage.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Film::getName).containsExactlyInAnyOrder("Film A", "Film B");
    }

    private static Film createFilm(String name, String description, LocalDate releaseDate, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        return film;
    }
}

package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    void validateFilmValidFilmShouldNotThrowException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertDoesNotThrow(() -> validationService.validateFilm(film));
    }

    @Test
    void validateFilmNullNameShouldThrowValidationException() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateFilm(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void validateFilmBlankNameShouldThrowValidationException() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateFilm(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void validateFilmDescriptionTooLongShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateFilm(film));
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void validateFilmReleaseDateTooEarlyShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateFilm(film));
        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void validateFilmNullReleaseDateShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(null);
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateFilm(film));
        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void validateFilmNegativeDurationShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-10);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateFilm(film));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void validateFilmZeroDurationShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateFilm(film));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void validateUserValidUserShouldNotThrowException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertDoesNotThrow(() -> validationService.validateUser(user));
    }

    @Test
    void validateUserNullEmailShouldThrowValidationException() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateUser(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void validateUserBlankEmailShouldThrowValidationException() {
        User user = new User();
        user.setEmail("   ");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateUser(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void validateUserEmailWithoutAtShouldThrowValidationException() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateUser(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void validateUserNullLoginShouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(null);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateUser(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void validateUserBlankLoginShouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("   ");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateUser(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void validateUserLoginWithSpacesShouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test user");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateUser(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void validateUserBlankNameShouldSetNameToLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("   ");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertDoesNotThrow(() -> validationService.validateUser(user));
        assertEquals("testuser", user.getName());
    }

    @Test
    void validateUserNullNameShouldSetNameToLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertDoesNotThrow(() -> validationService.validateUser(user));
        assertEquals("testuser", user.getName());
    }

    @Test
    void validateUserFutureBirthdayShouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateUser(user));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void validateUserNullBirthdayShouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> validationService.validateUser(user));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }
}
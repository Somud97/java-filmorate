package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MinReleaseDateValidatorTest {

    private MinReleaseDateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MinReleaseDateValidator();
    }

    @Test
    void isValidValidDateShouldReturnTrue() {
        LocalDate validDate = LocalDate.of(2000, 1, 1);

        boolean result = validator.isValid(validDate, null);

        assertTrue(result);
    }

    @Test
    void isValidFirstFilmDateShouldReturnTrue() {
        LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);

        boolean result = validator.isValid(firstFilmDate, null);

        assertTrue(result);
    }

    @Test
    void isValidDateBeforeFirstFilmShouldReturnFalse() {
        LocalDate beforeFirstFilm = LocalDate.of(1895, 12, 27);

        boolean result = validator.isValid(beforeFirstFilm, null);

        assertFalse(result);
    }

    @Test
    void isValidNullDateShouldReturnTrue() {
        boolean result = validator.isValid(null, null);

        assertTrue(result);
    }

    @Test
    void isValidCurrentDateShouldReturnTrue() {
        LocalDate currentDate = LocalDate.now();

        boolean result = validator.isValid(currentDate, null);

        assertTrue(result);
    }

    @Test
    void isValidFutureDateShouldReturnTrue() {
        LocalDate futureDate = LocalDate.now().plusYears(1);

        boolean result = validator.isValid(futureDate, null);

        assertTrue(result);
    }
}
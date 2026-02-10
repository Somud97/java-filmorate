package ru.yandex.practicum.filmorate.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {

    @Test
    void constructorWithMessageShouldSetMessage() {
        String message = "Test validation error";

        ValidationException exception = new ValidationException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithNullMessageShouldSetNullMessage() {
        ValidationException exception = new ValidationException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void constructorWithEmptyMessageShouldSetEmptyMessage() {
        String message = "";

        ValidationException exception = new ValidationException(message);

        assertEquals(message, exception.getMessage());
    }
}
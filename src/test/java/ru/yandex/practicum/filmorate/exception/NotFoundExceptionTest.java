package ru.yandex.practicum.filmorate.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    void constructorWithMessageShouldSetMessage() {
        String message = "Resource not found";

        NotFoundException exception = new NotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithNullMessageShouldSetNullMessage() {
        NotFoundException exception = new NotFoundException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void constructorWithEmptyMessageShouldSetEmptyMessage() {
        String message = "";

        NotFoundException exception = new NotFoundException(message);

        assertEquals(message, exception.getMessage());
    }
}
package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendLink;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, EventDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void add_shouldCreateUserAndReturnWithId() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user1");
        user.setName("User One");
        user.setBirthday(LocalDate.of(1990, 5, 15));

        User created = userStorage.add(user);

        assertThat(created.getId()).isPositive();
        assertThat(created.getEmail()).isEqualTo("user@mail.ru");
        assertThat(created.getLogin()).isEqualTo("user1");
        assertThat(created.getName()).isEqualTo("User One");
        assertThat(created.getBirthday()).isEqualTo(LocalDate.of(1990, 5, 15));
    }

    @Test
    void add_shouldSaveFriendLinks() {
        User user1 = userStorage.add(createUser("a@a.ru", "a", "A", LocalDate.of(2000, 1, 1)));
        User user2 = userStorage.add(createUser("b@b.ru", "b", "B", LocalDate.of(2001, 1, 1)));

        Set<FriendLink> links = new HashSet<>();
        links.add(new FriendLink(user2.getId(), FriendshipStatus.UNCONFIRMED));
        user1.setFriendLinks(links);
        userStorage.update(user1);

        User found = userStorage.findById(user1.getId());
        assertThat(found.getFriendLinks()).hasSize(1);
        assertThat(found.getFriendLinks().iterator().next().getFriendId()).isEqualTo(user2.getId());
        assertThat(found.getFriendLinks().iterator().next().getStatus()).isEqualTo(FriendshipStatus.UNCONFIRMED);
    }

    @Test
    void update_shouldUpdateUserAndReturnUpdated() {
        User user = userStorage.add(createUser("old@mail.ru", "old", "Old", LocalDate.of(1985, 3, 10)));
        user.setEmail("new@mail.ru");
        user.setLogin("newlogin");
        user.setName("New Name");

        User updated = userStorage.update(user);

        assertThat(updated.getEmail()).isEqualTo("new@mail.ru");
        assertThat(updated.getLogin()).isEqualTo("newlogin");
        assertThat(updated.getName()).isEqualTo("New Name");
        User found = userStorage.findById(user.getId());
        assertThat(found.getEmail()).isEqualTo("new@mail.ru");
    }

    @Test
    void update_shouldThrowWhenUserNotFound() {
        User user = new User();
        user.setId(99999);
        user.setEmail("x@x.ru");
        user.setLogin("x");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThatThrownBy(() -> userStorage.update(user))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    void delete_shouldRemoveUser() {
        User user = userStorage.add(createUser("del@mail.ru", "del", "Del", LocalDate.of(1995, 1, 1)));
        int id = user.getId();

        userStorage.deleteById(id);

        assertThatThrownBy(() -> userStorage.findById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(String.valueOf(id));
    }

    @Test
    void delete_shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> userStorage.deleteById(99999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    void findById_shouldReturnUserWhenExists() {
        User user = userStorage.add(createUser("find@mail.ru", "find", "Find Me", LocalDate.of(1992, 7, 20)));

        User found = userStorage.findById(user.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(user.getId());
        assertThat(found.getEmail()).isEqualTo("find@mail.ru");
        assertThat(found.getLogin()).isEqualTo("find");
        assertThat(found.getName()).isEqualTo("Find Me");
        assertThat(found.getBirthday()).isEqualTo(LocalDate.of(1992, 7, 20));
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> userStorage.findById(99999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoUsers() {
        List<User> all = userStorage.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userStorage.add(createUser("u1@mail.ru", "u1", "U1", LocalDate.of(1990, 1, 1)));
        userStorage.add(createUser("u2@mail.ru", "u2", "U2", LocalDate.of(1991, 1, 1)));

        List<User> all = userStorage.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(User::getLogin).containsExactlyInAnyOrder("u1", "u2");
    }

    private static User createUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }
}

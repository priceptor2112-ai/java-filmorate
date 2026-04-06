package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @GetMapping
    public List<User> findAll() {
        log.debug("Запрос на получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    // ДОБАВИТЬ ЭТОТ МЕТОД ДЛЯ СОВМЕСТИМОСТИ С ТЕСТАМИ
    public List<User> getAllUsers() {
        return findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        log.info("Запрос на создание пользователя: {}", user);
        validateUser(user);
        user.setId(nextId++);
        setNameFromLoginIfBlank(user);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с id={}", user.getId());
        return user;
    }

    // ДОБАВИТЬ ЭТОТ МЕТОД ДЛЯ СОВМЕСТИМОСТИ С ТЕСТАМИ
    public User createUser(User user) {
        return create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Запрос на обновление пользователя с id={}", user.getId());
        if (user.getId() == null) {
            log.error("ID пользователя не может быть null при обновлении");
            throw new ValidationException("ID пользователя должен быть указан");
        }
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id={} не найден", user.getId());
            throw new ValidationException(
                    "Пользователь с id " + user.getId() + " не найден");
        }
        validateUser(user);
        setNameFromLoginIfBlank(user);
        users.put(user.getId(), user);
        log.info("Пользователь с id={} успешно обновлён", user.getId());
        return user;
    }

    // ДОБАВИТЬ ЭТОТ МЕТОД ДЛЯ СОВМЕСТИМОСТИ С ТЕСТАМИ
    public User updateUser(User user) {
        return update(user);
    }

    private void validateUser(User user) {
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения '{}' в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void setNameFromLoginIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя установлено как логин: {}", user.getLogin());
        }
    }
}
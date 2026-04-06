package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final Map<Long, Film> films = new HashMap<>();
    private Long nextId = 1L;

    @GetMapping
    public List<Film> findAll() {
        log.debug("Запрос на получение всех фильмов");
        return new ArrayList<>(films.values());
    }

    // ДОБАВИТЬ ЭТОТ МЕТОД ДЛЯ СОВМЕСТИМОСТИ С ТЕСТАМИ
    public List<Film> getAllFilms() {
        return findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody Film film) {
        log.info("Запрос на добавление фильма: {}", film);
        validateReleaseDate(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен с id={}", film.getId());
        return film;
    }

    // ДОБАВИТЬ ЭТОТ МЕТОД ДЛЯ СОВМЕСТИМОСТИ С ТЕСТАМИ
    public Film createFilm(Film film) {
        return create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Запрос на обновление фильма с id={}", film.getId());
        if (film.getId() == null) {
            log.error("ID фильма не может быть null при обновлении");
            throw new ValidationException("ID фильма должен быть указан");
        }
        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id={} не найден", film.getId());
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }
        validateReleaseDate(film);
        films.put(film.getId(), film);
        log.info("Фильм с id={} успешно обновлён", film.getId());
        return film;
    }

    // ДОБАВИТЬ ЭТОТ МЕТОД ДЛЯ СОВМЕСТИМОСТИ С ТЕСТАМИ
    public Film updateFilm(Film film) {
        return update(film);
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null
                && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза {} не может быть раньше 28 декабря 1895 года",
                    film.getReleaseDate());
            throw new ValidationException(
                    "Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
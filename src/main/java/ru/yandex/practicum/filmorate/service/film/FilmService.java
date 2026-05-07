package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private LikeDbStorage likeStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       LikeDbStorage likeDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeStorage = likeDbStorage;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film != null) {
            if (userStorage.getUserById(userId) != null) {
                likeStorage.addLike(filmId, userId);
            } else {
                throw new UserNotFoundException("Пользователь c ID=" + userId + " не найден!");
            }
        } else {
            throw new FilmNotFoundException("Фильм c ID=" + filmId + " не найден!");
        }
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film != null) {
            if (film.getLikes().contains(userId)) {
                likeStorage.deleteLike(filmId, userId);
            } else {
                throw new UserNotFoundException("Лайк от пользователя c ID=" + userId + " не найден!");
            }
        } else {
            throw new FilmNotFoundException("Фильм c ID=" + filmId + " не найден!");
        }
    }

    public List<Film> getPopular(Integer count) {
        if (count < 1) {
            throw new ValidationException("Количество фильмов для вывода не должно быть меньше 1");
        }
        return likeStorage.getPopular(count);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film delete(Long filmId) {
        return filmStorage.delete(filmId);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        // Проверяем, что оба пользователя существуют
        try {
            userStorage.getUserById(userId);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Пользователь c ID=" + userId + " не найден!");
        }
        try {
            userStorage.getUserById(friendId);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("Пользователь c ID=" + friendId + " не найден!");
        }

        List<Film> allFilms = filmStorage.getFilms();

        Set<Long> userLikes = allFilms.stream()
                .filter(film -> film.getLikes() != null && film.getLikes().contains(userId))
                .map(Film::getId)
                .collect(Collectors.toSet());

        Set<Long> friendLikes = allFilms.stream()
                .filter(film -> film.getLikes() != null && film.getLikes().contains(friendId))
                .map(Film::getId)
                .collect(Collectors.toSet());

        Set<Long> commonFilmIds = new HashSet<>(userLikes);
        commonFilmIds.retainAll(friendLikes);

        return commonFilmIds.stream()
                .map(filmStorage::getFilmById)
                .filter(Objects::nonNull)
                .sorted((f1, f2) -> {
                    int size1 = f1.getLikes() != null ? f1.getLikes().size() : 0;
                    int size2 = f2.getLikes() != null ? f2.getLikes().size() : 0;
                    return Integer.compare(size2, size1);
                })
                .collect(Collectors.toList());
    }
}
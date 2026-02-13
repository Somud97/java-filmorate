package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaaRating;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }
        film.setDuration(rs.getInt("duration"));
        Object mpaaRatingIdObj = rs.getObject("mpaa_rating_id");
        if (mpaaRatingIdObj != null) {
            film.setMpaaRatingId(rs.getInt("mpaa_rating_id"));
        }
        String mpaaCode = rs.getString("mpaa_code");
        if (mpaaCode != null) {
            film.setMpaaRating(parseMpaaRating(mpaaCode));
        }
        return film;
    };

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpaa_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        Integer mpaaRatingId = resolveMpaaRatingId(film);

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            if (mpaaRatingId != null && mpaaRatingId > 0) {
                ps.setInt(5, mpaaRatingId);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKey() != null ? keyHolder.getKey().intValue() : null;
        if (id == null) {
            throw new RuntimeException("Не удалось получить ID созданного фильма");
        }
        film.setId(id);

        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            saveGenresByIds(film.getId(), film.getGenreIds());
        } else {
            saveGenres(film.getId(), film.getGenres());
        }
        saveLikes(film.getId(), film.getLikes());

        return findById(id);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpaa_rating_id = ? WHERE id = ?";
        Integer mpaaRatingId = resolveMpaaRatingId(film);

        int rowsAffected = jdbcTemplate.update(sql,
            film.getName(),
            film.getDescription(),
            film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
            film.getDuration(),
            (mpaaRatingId != null && mpaaRatingId > 0) ? mpaaRatingId : null,
            film.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        deleteGenres(film.getId());
        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            saveGenresByIds(film.getId(), film.getGenreIds());
        } else {
            saveGenres(film.getId(), film.getGenres());
        }
        deleteLikes(film.getId());
        saveLikes(film.getId(), film.getLikes());

        return findById(film.getId());
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM films WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
    }

    @Override
    public Film findById(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpaa_rating_id, " +
            "mr.code AS mpaa_code FROM films f " +
            "LEFT JOIN mpaa_ratings mr ON f.mpaa_rating_id = mr.id WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        Film film = films.get(0);

        film.setGenres(loadGenres(id));
        film.setGenreIds(loadGenreIds(id));
        film.setGenresResponse(loadGenresDto(id));
        film.setLikes(loadLikes(id));

        return film;
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpaa_rating_id, " +
            "mr.code AS mpaa_code FROM films f " +
            "LEFT JOIN mpaa_ratings mr ON f.mpaa_rating_id = mr.id";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        for (Film film : films) {
            film.setGenres(loadGenres(film.getId()));
            film.setGenreIds(loadGenreIds(film.getId()));
            film.setGenresResponse(loadGenresDto(film.getId()));
            film.setLikes(loadLikes(film.getId()));
        }
        return films;
    }

    private Set<Genre> loadGenres(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres g JOIN film_genre fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql,
            (rs, rowNum) -> {
                String name = rs.getString("name");
                for (Genre genre : Genre.values()) {
                    if (genre.getName().equals(name)) {
                        return genre;
                    }
                }
                return null;
            },
            filmId);
        return genres.stream().filter(g -> g != null).collect(Collectors.toSet());
    }

    private List<Integer> loadGenreIds(int filmId) {
        String sql = "SELECT genre_id FROM film_genre WHERE film_id = ? ORDER BY genre_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("genre_id"), filmId);
    }

    private List<GenreDto> loadGenresDto(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres g JOIN film_genre fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new GenreDto(rs.getInt("id"), rs.getString("name")), filmId);
    }

    private Set<Integer> loadLikes(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.query(sql,
            (rs, rowNum) -> rs.getInt("user_id"),
            filmId);
        return new HashSet<>(likes);
    }

    private void saveGenres(int filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        Map<String, Integer> nameToId = getGenreNameToIdMap();
        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : genres) {
            Integer genreId = nameToId.get(genre.getName());
            if (genreId != null) {
                batchArgs.add(new Object[]{filmId, genreId});
            }
        }
        if (!batchArgs.isEmpty()) {
            String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void saveGenresByIds(int filmId, List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }
        Set<Integer> uniqueIds = new HashSet<>(genreIds);
        List<Object[]> batchArgs = new ArrayList<>();
        for (Integer genreId : uniqueIds) {
            if (genreId != null && genreId > 0) {
                batchArgs.add(new Object[]{filmId, genreId});
            }
        }
        if (!batchArgs.isEmpty()) {
            String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void saveLikes(int filmId, Set<Integer> likes) {
        if (likes == null || likes.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>(likes.size());
        for (Integer userId : likes) {
            batchArgs.add(new Object[]{filmId, userId});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private Map<String, Integer> getGenreNameToIdMap() {
        String sql = "SELECT id, name FROM genres";
        Map<String, Integer> map = new HashMap<>();
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            map.put(rs.getString("name"), rs.getInt("id"));
            return null;
        });
        return map;
    }

    private void deleteGenres(int filmId) {
        String sql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private void deleteLikes(int filmId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private Integer getMpaaRatingId(MpaaRating rating) {
        String sql = "SELECT id FROM mpaa_ratings WHERE code = ?";
        List<Integer> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("id"), rating.getCode());
        return ids.isEmpty() ? null : ids.get(0);
    }

    private Integer getGenreId(Genre genre) {
        String sql = "SELECT id FROM genres WHERE name = ?";
        List<Integer> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("id"), genre.getName());
        return ids.isEmpty() ? null : ids.get(0);
    }

    private Integer resolveMpaaRatingId(Film film) {
        Integer id = film.getMpaaRatingId();
        if (id != null && id > 0) {
            return id;
        }
        if (film.getMpaaRating() != null) {
            return getMpaaRatingId(film.getMpaaRating());
        }
        return null;
    }

    private MpaaRating parseMpaaRating(String code) {
        if (code == null) return null;
        for (MpaaRating rating : MpaaRating.values()) {
            if (rating.getCode().equals(code)) {
                return rating;
            }
        }
        return null;
    }
}

package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaaRating;
import ru.yandex.practicum.filmorate.model.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;
import ru.yandex.practicum.filmorate.validation.ValidationUtils;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ValidationUtils validationUtils;

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
        validationUtils.validateFilmInTheFuture(film);
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

        if (film.getDirectorIds() != null && !film.getDirectorIds().isEmpty()) {
            saveDirectors(film.getId(), film.getDirectorIds());
        } else if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            saveDirectorsFromDto(film.getId(), film.getDirectors());
        }

        saveLikes(film.getId(), film.getLikes());

        return findById(id);
    }

    @Override
    public Film update(Film film) {
        validationUtils.validateFilm(film.getId());
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

        deleteDirectors(film.getId());
        if (film.getDirectorIds() != null && !film.getDirectorIds().isEmpty()) {
            saveDirectors(film.getId(), film.getDirectorIds());
        } else if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            saveDirectorsFromDto(film.getId(), film.getDirectors());
        }

        deleteLikes(film.getId());
        saveLikes(film.getId(), film.getLikes());

        return findById(film.getId());
    }

    @Override
    public void deleteById(int id) {
        validationUtils.validateFilm(id);
        String sql = "DELETE FROM films WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
    }

    @Override
    public Film findById(int id) {
        validationUtils.validateFilm(id);

        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpaa_rating_id, " +
                "mr.code AS mpaa_code FROM films f " +
                "LEFT JOIN mpaa_ratings mr ON f.mpaa_rating_id = mr.id WHERE f.id = ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }

        Film film = films.get(0);
        film.setGenreIds(loadGenreIds(id));
        film.setGenresResponse(loadGenresDto(id));
        film.setDirectorIds(loadDirectorIds(id));
        film.setDirectors(loadDirectors(id));
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

            film.setDirectorIds(loadDirectorIds(film.getId()));
            film.setDirectors(loadDirectors(film.getId()));

            film.setLikes(loadLikes(film.getId()));
        }
        return films;
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        String sql;
        Object[] params;

        if ("year".equals(sortBy)) {
            sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpaa_rating_id, " +
                    "mr.code AS mpaa_code FROM films f " +
                    "LEFT JOIN mpaa_ratings mr ON f.mpaa_rating_id = mr.id " +
                    "JOIN film_director fd ON f.id = fd.film_id " +  // film_director
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date";
            params = new Object[]{directorId};
        } else {
            sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpaa_rating_id, " +
                    "mr.code AS mpaa_code, COUNT(fl.user_id) as likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpaa_ratings mr ON f.mpaa_rating_id = mr.id " +
                    "JOIN film_director fd ON f.id = fd.film_id " +  // film_director
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpaa_rating_id, mr.code " +
                    "ORDER BY likes_count DESC";
            params = new Object[]{directorId};
        }

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, params);
        loadGenresAndDirectorsForFilms(films);
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

    private void loadGenresAndDirectorsForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        List<Integer> filmIds = new ArrayList<>(filmMap.keySet());

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));

        String genresSql = "SELECT fg.film_id, g.id, g.name FROM film_genre fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + placeholders + ") ORDER BY g.id";

        for (Film film : films) {
            if (film.getGenreIds() == null) {
                film.setGenreIds(new ArrayList<>());
            }
            if (film.getGenresResponse() == null) {
                film.setGenresResponse(new ArrayList<>());
            }
            if (film.getGenres() == null) {
                film.setGenres(new HashSet<>());
            }
        }

        jdbcTemplate.query(genresSql, filmIds.toArray(), rs -> {
            Film film = filmMap.get(rs.getInt("film_id"));
            if (film != null) {
                int genreId = rs.getInt("id");
                String genreName = rs.getString("name");

                film.getGenreIds().add(genreId);

                film.getGenresResponse().add(new GenreDto(genreId, genreName));

                for (Genre genre : Genre.values()) {
                    if (genre.getName().equals(genreName)) {
                        film.getGenres().add(genre);
                        break;
                    }
                }
            }
        });

        String directorsSql = "SELECT fd.film_id, d.id, d.name FROM film_director fd " +
                "JOIN directors d ON fd.director_id = d.id " +
                "WHERE fd.film_id IN (" + placeholders + ") ORDER BY d.id";

        for (Film film : films) {
            if (film.getDirectorIds() == null) {
                film.setDirectorIds(new HashSet<>());
            }
            if (film.getDirectors() == null) {
                film.setDirectors(new ArrayList<>());
            }
        }

        jdbcTemplate.query(directorsSql, filmIds.toArray(), rs -> {
            Film film = filmMap.get(rs.getInt("film_id"));
            if (film != null) {
                int directorId = rs.getInt("id");
                String directorName = rs.getString("name");

                film.getDirectorIds().add(directorId);

                film.getDirectors().add(new DirectorDto(directorId, directorName));
            }
        });

        String likesSql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + placeholders + ")";

        for (Film film : films) {
            if (film.getLikes() == null) {
                film.setLikes(new HashSet<>());
            }
        }

        jdbcTemplate.query(likesSql, filmIds.toArray(), rs -> {
            Film film = filmMap.get(rs.getInt("film_id"));
            if (film != null) {
                film.getLikes().add(rs.getInt("user_id"));
            }
        });
    }

    private Set<Integer> loadDirectorIds(int filmId) {
        String sql = "SELECT director_id FROM film_director WHERE film_id = ? ORDER BY director_id";
        List<Integer> directorIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("director_id"), filmId);
        return new HashSet<>(directorIds);
    }

    private List<DirectorDto> loadDirectors(int filmId) {
        String sql = "SELECT d.id, d.name FROM directors d " +
                "JOIN film_director fd ON d.id = fd.director_id " +
                "WHERE fd.film_id = ? ORDER BY d.id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new DirectorDto(rs.getInt("id"), rs.getString("name")), filmId);
    }

    private void saveDirectors(int filmId, Set<Integer> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) return;

        String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        for (Integer directorId : directorIds) {
            batchArgs.add(new Object[]{filmId, directorId});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void saveDirectorsFromDto(int filmId, List<DirectorDto> directors) {
        if (directors == null || directors.isEmpty()) return;

        String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        for (DirectorDto director : directors) {
            batchArgs.add(new Object[]{filmId, director.getId()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void deleteDirectors(int filmId) {
        String sql = "DELETE FROM film_director WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    public List<Film> search(String query, String by) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }

        String searchPattern = "%" + query.toLowerCase() + "%";
        Set<Film> uniqueFilms = new HashSet<>();

        if ("title".equals(by) || "title,director".equals(by)) {
            String titleSql = "SELECT f.*, mr.code AS mpaa_code " +
                    "FROM films f " +
                    "LEFT JOIN mpaa_ratings mr ON f.mpaa_rating_id = mr.id " +
                    "WHERE LOWER(f.name) LIKE ?";

            List<Film> titleFilms = jdbcTemplate.query(titleSql, filmRowMapper, searchPattern);
            uniqueFilms.addAll(titleFilms);
        }

        if ("director".equals(by) || "title,director".equals(by)) {
            String directorSql = "SELECT f.*, mr.code AS mpaa_code " +
                    "FROM films f " +
                    "LEFT JOIN mpaa_ratings mr ON f.mpaa_rating_id = mr.id " +
                    "JOIN film_director fd ON f.id = fd.film_id " +
                    "JOIN directors d ON fd.director_id = d.id " +
                    "WHERE LOWER(d.name) LIKE ?";

            List<Film> directorFilms = jdbcTemplate.query(directorSql, filmRowMapper, searchPattern);
            uniqueFilms.addAll(directorFilms);
        }

        List<Film> films = new ArrayList<>(uniqueFilms);

        loadGenresAndDirectorsForFilms(films);

        films.sort((f1, f2) -> {
            int compare = Integer.compare(
                    f2.getLikes().size(),
                    f1.getLikes().size()
            );
            if (compare == 0) {
                return Integer.compare(f1.getId(), f2.getId());
            }
            return compare;
        });

        films.sort((f1, f2) -> f2.getLikes().size() - f1.getLikes().size());
        return films;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpaa_rating_id, " +
                "mr.code AS mpaa_code, COUNT(DISTINCT fl.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN mpaa_ratings mr ON f.mpaa_rating_id = mr.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "WHERE f.id IN (" +
                "    SELECT fl1.film_id " +
                "    FROM film_likes fl1 " +
                "    WHERE fl1.user_id = ? " +
                "    INTERSECT " +
                "    SELECT fl2.film_id " +
                "    FROM film_likes fl2 " +
                "    WHERE fl2.user_id = ?" +
                ") " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpaa_rating_id, mr.code " +
                "ORDER BY likes_count DESC, f.id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, userId, friendId);

        loadGenresAndDirectorsForFilms(films);

        return films;
    }
}
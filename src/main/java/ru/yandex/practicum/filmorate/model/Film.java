package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.dto.MpaaDto;
import ru.yandex.practicum.filmorate.validation.MinReleaseDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class Film {
    private int id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @MinReleaseDate(message = "Дата релиза — не раньше 28 декабря 1895 года")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

    private Set<Genre> genres = new HashSet<>();

    private List<Integer> genreIds = new ArrayList<>();

    private MpaaRating mpaaRating;

    private Integer mpaaRatingId;

    private Set<Integer> likes = new HashSet<>();

    private List<GenreDto> genresResponse = new ArrayList<>();

    @JsonSetter("mpa")
    public void setMpaFromJson(MpaaDto mpa) {
        if (mpa != null) {
            this.mpaaRatingId = mpa.getId();
        }
    }

    @JsonSetter("genres")
    public void setGenresFromJson(List<GenreDto> genresDto) {
        if (genresDto != null) {
            this.genreIds = genresDto.stream().map(GenreDto::getId).collect(Collectors.toList());
        }
    }

    @JsonProperty("mpa")
    public MpaaDto getMpaForJson() {
        if (mpaaRatingId == null && mpaaRating == null) return null;
        String name = mpaaRating != null ? mpaaRating.getCode() : null;
        if (mpaaRatingId != null && name != null) {
            return new MpaaDto(mpaaRatingId, name);
        }
        if (mpaaRatingId != null) return new MpaaDto(mpaaRatingId, "");
        return null;
    }

    @JsonIgnore
    public Set<Genre> getGenres() {
        return genres;
    }

    @JsonProperty("genres")
    public List<GenreDto> getGenresForJson() {
        return genresResponse != null ? genresResponse : new ArrayList<>();
    }
}

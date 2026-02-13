package ru.yandex.practicum.filmorate.model;

public enum MpaaRating {
    G("G — у фильма нет возрастных ограничений"),
    PG("PG — детям рекомендуется смотреть фильм с родителями"),
    PG_13("PG-13 — детям до 13 лет просмотр не желателен"),
    R("R — лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
    NC_17("NC-17 — лицам до 18 лет просмотр запрещён");

    private final String description;

    MpaaRating(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /** Код рейтинга для API (G, PG, PG-13, R, NC-17). */
    public String getCode() {
        int sep = description.indexOf(" — ");
        return sep >= 0 ? description.substring(0, sep) : description.split(" ")[0];
    }
}

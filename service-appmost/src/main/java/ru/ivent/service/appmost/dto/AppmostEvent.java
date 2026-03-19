package ru.ivent.service.appmost.dto;

import com.google.gson.annotations.SerializedName;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Laughina
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppmostEvent {

    @SerializedName("id")
    long id;

    @SerializedName("name")
    String name;

    @SerializedName("type")
    String type;

    @SerializedName("description")
    String description;

    @SerializedName("poster")
    String poster;

    @SerializedName("link")
    String link;

    @SerializedName("afisha_type")
    String afishaType;

    @SerializedName("afisha_type_slug")
    String afishaTypeSlug;

    @SerializedName("genres")
    String genres;

    @SerializedName("end_date")
    String endDate;

    @SerializedName("min_price")
    Long minPriceKopecks;

    @SerializedName("age_rating")
    String ageRating;

    @SerializedName("duration")
    Integer durationMinutes;
}

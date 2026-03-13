package ru.ivent.service.appmost.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

import java.util.List;

/**
 * @author Laughina
 */
@Data
public class AppmostEvent {

    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("poster")
    private String poster;

    /** Short URL slug or full link */
    @SerializedName("url")
    private String url;

    @SerializedName("place")
    private AppmostPlace place;

    @SerializedName("category")
    private AppmostCategory category;

    /** Unix timestamp (seconds) */
    @SerializedName("date_start")
    private Long dateStart;

    /** Unix timestamp (seconds) */
    @SerializedName("date_end")
    private Long dateEnd;

    @SerializedName("min_price")
    private Double minPrice;

    @SerializedName("age_restriction")
    private String ageRestriction;

    @SerializedName("tags")
    private List<String> tags;

    @Data
    public static class AppmostPlace {

        @SerializedName("id")
        private long id;

        @SerializedName("title")
        private String title;

        @SerializedName("address")
        private String address;
    }

    @Data
    public static class AppmostCategory {

        @SerializedName("id")
        private long id;

        @SerializedName("title")
        private String title;

        @SerializedName("alias")
        private String alias;
    }
}

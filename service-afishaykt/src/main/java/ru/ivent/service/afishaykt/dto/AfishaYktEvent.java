package ru.ivent.service.afishaykt.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

import java.util.List;

/**
 * @author Laughina
 */
@Data
public class AfishaYktEvent {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("genre")
    private String genre;

    @SerializedName("poster")
    private AfishaYktPoster poster;

    @SerializedName("seances")
    private List<AfishaYktSeance> seances;

    @SerializedName("buyButtonLink")
    private String buyButtonLink;

    @SerializedName("videoUrl")
    private String videoUrl;

    @Data
    public static class AfishaYktPoster {
        @SerializedName("h200")
        private String h200;

        @SerializedName("h400")
        private String h400;

        @SerializedName("original")
        private String original;

        @SerializedName("valid")
        private boolean valid;
    }

    @Data
    public static class AfishaYktSeance {

        @SerializedName("id")
        private long id;

        @SerializedName("dateTimeUnix")
        private Long dateTimeUnix;

        @SerializedName("dateString")
        private String dateString;

        @SerializedName("date")
        private String date;

        @SerializedName("time")
        private String time;

        @SerializedName("companyName")
        private String companyName;

        @SerializedName("price")
        private String price;

        @SerializedName("saleUrl")
        private String saleUrl;

        @SerializedName("buyButtonLink")
        private String buyButtonLink;

        @SerializedName("isSaleOpen")
        private boolean saleOpen;

        @SerializedName("time_late")
        private boolean timeLate;
    }
}

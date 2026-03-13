package ru.ivent.service.afishaykt.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

import java.util.List;

/**
 * @author Laughina
 */
@Data
public class AfishaYktResponse {

    @SerializedName("events")
    private List<AfishaYktEvent> events;

    @SerializedName("alldates")
    private List<String> allDates;

    @SerializedName("response")
    private AfishaYktStatus response;

    @Data
    public static class AfishaYktStatus {
        @SerializedName("code")
        private int code;

        @SerializedName("text")
        private String text;
    }
}

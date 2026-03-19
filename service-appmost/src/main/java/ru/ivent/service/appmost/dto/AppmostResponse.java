package ru.ivent.service.appmost.dto;

import com.google.gson.annotations.SerializedName;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author Laughina
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppmostResponse {

    @SerializedName("data")
    List<AppmostEvent> data;

    @SerializedName("items")
    List<AppmostEvent> items;

    @SerializedName("success")
    boolean success;

    @SerializedName("meta")
    Meta meta;

    public List<AppmostEvent> getEvents() {
        if (data != null && !data.isEmpty()) return data;
        if (items != null && !items.isEmpty()) return items;
        return List.of();
    }

    public int getLastPage() {
        return (meta != null && meta.lastPage > 0) ? meta.lastPage : 1;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Meta {

        @SerializedName("current_page")
        int currentPage;

        @SerializedName("last_page")
        int lastPage;

        @SerializedName("per_page")
        int perPage;

        @SerializedName("total")
        int total;
    }
}

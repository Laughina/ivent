package ru.ivent.service.appmost.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @author Laughina
 */
@Data
public class AppmostResponse {

    @SerializedName("data")
    private List<AppmostEvent> data;

    @SerializedName("success")
    private boolean success;
}

package ru.bozhov.waterlevelbot.sensor.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder(setterPrefix = "with")
public class SensorRegResponse {
    @JsonProperty("message")
    private String message;

    public static SensorRegResponse fromMessage(String message){
        return SensorRegResponse
                .builder()
                .withMessage(message)
                .build();
    }
}

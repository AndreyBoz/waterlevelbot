package ru.bozhov.waterlevelbot.sensor.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.annotation.Nonnull;

@Data
public class SensorRegRequest {
    @Nonnull
    @JsonProperty("sensor_name")
    private String sensorName;

    @Nonnull
    @JsonProperty("time_zone")
    private String timeZone;
}

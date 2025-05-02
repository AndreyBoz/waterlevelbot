package ru.bozhov.waterlevelbot.sensor.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SensorRegRequest {
    @JsonProperty("sensor_name")
    private String sensorName;
}

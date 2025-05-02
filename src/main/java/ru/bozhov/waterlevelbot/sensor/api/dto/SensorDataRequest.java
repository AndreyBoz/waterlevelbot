package ru.bozhov.waterlevelbot.sensor.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SensorDataRequest {
    @JsonProperty("water_level")
    private Float waterLevel;

    @JsonProperty("sensor_name")
    private String sensorName;

    @JsonProperty("temperature")
    private Float temperature;

    @JsonProperty("humidity")
    private Float humidity;

    @JsonProperty("time")
    private String time;
}

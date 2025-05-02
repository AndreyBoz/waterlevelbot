package ru.bozhov.waterlevelbot.sensor.mapper;

import org.mapstruct.Mapper;
import ru.bozhov.waterlevelbot.sensor.api.dto.SensorDataRequest;
import ru.bozhov.waterlevelbot.sensor.model.SensorData;


@Mapper
public interface  SensorMapper {
    SensorData sensorDataRequestToSensorData(SensorDataRequest sensorDataRequest);

}

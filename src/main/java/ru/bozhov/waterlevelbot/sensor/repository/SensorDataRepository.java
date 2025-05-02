package ru.bozhov.waterlevelbot.sensor.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    List<SensorData> findBySensor(Sensor sensor);

    List<SensorData> findBySensorAndLocalDateTimeBetween(Sensor sensor, LocalDateTime start, LocalDateTime end);

    Optional<SensorData> findTopBySensorIdOrderByLocalDateTimeDesc(Long sensorId);
}

package ru.bozhov.waterlevelbot.sensor.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.bozhov.waterlevelbot.sensor.api.dto.SensorDataRequest;
import ru.bozhov.waterlevelbot.sensor.api.dto.SensorRegRequest;
import ru.bozhov.waterlevelbot.sensor.api.dto.SensorRegResponse;
import ru.bozhov.waterlevelbot.sensor.mapper.SensorMapper;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorData;
import ru.bozhov.waterlevelbot.sensor.model.SensorStatus;
import ru.bozhov.waterlevelbot.sensor.repository.SensorDataRepository;
import ru.bozhov.waterlevelbot.sensor.repository.SensorRepository;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@AllArgsConstructor
@Service
public class SensorDataService {
    private final SensorMapper sensorMapper = Mappers.getMapper(SensorMapper.class);

    private SensorRepository sensorRepository;
    private SensorDataRepository sensorDataRepository;
    private TelegramService telegramService;
    private TelegramUserService telegramUserService;
    private BotService botService;

    @Transactional
    public ResponseEntity<SensorRegResponse> registrationSensor(SensorRegRequest sensorData) {
        requireNonNull(sensorData);
        Optional<Sensor> optionalSensor = sensorRepository.findSensorBySensorName(sensorData.getSensorName());

        if (optionalSensor.isPresent()) {
            Sensor sensor = optionalSensor.get();
            if (optionalSensor.get().getSensorStatus().equals(SensorStatus.GET_DATA) && Objects.equals(sensor.getTimeZone(), sensor.getTimeZone())) {
                return ResponseEntity.ok(SensorRegResponse.fromMessage(getCurrentTime()));
            }
            sensor.setSensorStatus(SensorStatus.GET_DATA);
            sensor.setTimeZone(sensor.getTimeZone());
            sensorRepository.save(sensor);

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().body(SensorRegResponse.fromMessage("ERROR"));
    }

    public SensorData getLastMeasure(Sensor sensor) {
        return sensorDataRepository
                .findTopBySensorIdOrderByLocalDateTimeDesc(sensor.getId())
                .orElse(null);
    }

    private String getCurrentTime() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public ResponseEntity<?> acceptData(SensorDataRequest request) {
        Optional<Sensor> sensorOpt = sensorRepository.findSensorBySensorName(request.getSensorName());
        if (sensorOpt.isEmpty()) {
            log.error("Ошибка при сохранении данных с датчика: датчик '{}' не найден",
                    request.getSensorName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }


        Sensor sensor = sensorOpt.get();
        SensorData sensorData = new SensorData();
        sensorData.setSensor(sensor);
        if (request.getHumidity() != null)
            sensorData.setHumidity(request.getHumidity());
        if (request.getTemperature() != null)
            sensorData.setTemperature(request.getTemperature());
        if (request.getWaterLevel() != null)
            sensorData.setWaterLevel(request.getWaterLevel());

        try {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(request.getTime(), formatter);
            sensorData.setLocalDateTime(dateTime);
        } catch (DateTimeParseException ex) {
            log.error("Ошибка парсинга времени '{}': {}", request.getTime(), ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        sensorDataRepository.save(sensorData);

        if (sensorData.getWaterLevel()!=null && isCriticalLevel(request.getWaterLevel(), sensorOpt.get())) {
            botService.sendCriticalLevelMessage(sensorOpt.get(), sensorData.getWaterLevel());
        }else {
            botService.sendMessageForSubscribers(sensorOpt.get(), sensorData);
        }

        return ResponseEntity.ok().build();
    }

    private boolean isCriticalLevel(Float waterLevel, Sensor sensor) {
        return sensor.getNormalLevel() != null && sensor.getNormalLevel() > waterLevel;
    }
}
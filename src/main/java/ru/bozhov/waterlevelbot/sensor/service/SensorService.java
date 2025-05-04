package ru.bozhov.waterlevelbot.sensor.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bozhov.waterlevelbot.sensor.model.Coordinate;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorAddress;
import ru.bozhov.waterlevelbot.sensor.model.SensorStatus;
import ru.bozhov.waterlevelbot.sensor.repository.SensorRepository;
import ru.bozhov.waterlevelbot.sensor.utils.EncodeUtils;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SensorService {

    private SensorRepository sensorRepository;
    private TelegramUserService telegramUserService;

    public Sensor createNewSensor(TelegramUser user) {

        Sensor sensor = new Sensor();
        sensor.addAdmin(user);
        sensor = sensorRepository.save(sensor);
        sensor.setSensorName(EncodeUtils.encodeBase52(sensor.getId() + user.getChatId()));

        return sensorRepository.save(sensor);
    }

    @Transactional
    public void editSensorAddress(Sensor sensor, SensorAddress sensorAddress, TelegramUser user) {
        sensor.setAddress(sensorAddress);
        sensorRepository.save(sensor);
        telegramUserService.changeBotState(user, BotState.IDLE);
    }

    public Optional<Sensor> findSensorBySensorName(String sensorName) {
        return sensorRepository.findSensorBySensorName(sensorName);
    }

    public void subscribeSensor(TelegramUser user, Sensor sensor){
        sensor.addSubscriber(user);
        sensorRepository.save(sensor);
    }


    public Optional<Sensor> findAwaitingLocationSensorByAdmin(TelegramUser user) {
        return sensorRepository.findSensorByAdminAndStatus(List.of(user), SensorStatus.AWAITING_LOCATION);
    }

    public List<Sensor> getSensorsForAdmin(TelegramUser user) {
        return sensorRepository.findAll().stream()
                .filter(sensor -> sensor.getAdmins().contains(user))
                .collect(Collectors.toList());
    }

    @Transactional
    public void setCoordinate(Sensor sensor, Coordinate coordinate) {
        sensor.setCoordinate(coordinate);
        sensorRepository.save(sensor);
    }

    public void setNormalLevel(Sensor sensor, Float normalLevel){
        sensor.setNormalLevel(normalLevel);
        sensorRepository.save(sensor);
    }

    public Sensor findBySensorAddress(SensorAddress address) {
        return sensorRepository.findSensorByAddress(address);
    }


}

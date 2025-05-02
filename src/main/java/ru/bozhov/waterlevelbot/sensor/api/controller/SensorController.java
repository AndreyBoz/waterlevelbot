package ru.bozhov.waterlevelbot.sensor.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bozhov.waterlevelbot.sensor.api.dto.SensorDataRequest;
import ru.bozhov.waterlevelbot.sensor.api.dto.SensorRegRequest;
import ru.bozhov.waterlevelbot.sensor.api.dto.SensorRegResponse;
import ru.bozhov.waterlevelbot.sensor.service.SensorDataService;


@RestController
@RequestMapping("/api/sensor")
public class SensorController {
    private final SensorDataService sensorService;


    public SensorController(SensorDataService sensorService) {
        this.sensorService = sensorService;
    }

    @PostMapping("/reg")
    public ResponseEntity<SensorRegResponse> regSensor(@RequestBody @Valid SensorRegRequest sensorData) {
        return sensorService.registrationSensor(sensorData);
    }

    @PostMapping("/data")
    public ResponseEntity<?> acceptData(@RequestBody @Valid SensorDataRequest request){
        return sensorService.acceptData(request);
    }

}

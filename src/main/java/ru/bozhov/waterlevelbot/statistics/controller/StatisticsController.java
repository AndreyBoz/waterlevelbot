package ru.bozhov.waterlevelbot.statistics.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorData;
import ru.bozhov.waterlevelbot.sensor.model.SensorStatus;
import ru.bozhov.waterlevelbot.sensor.repository.SensorDataRepository;
import ru.bozhov.waterlevelbot.sensor.service.SensorService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class StatisticsController {

    private final SensorService sensorService;
    private final SensorDataRepository sensorDataRepository;

    @GetMapping("/statistics")
    public ModelAndView showStatisticsForm(
            @RequestParam(value = "sensorName", required = false) String sensorName) {
        ModelAndView mav = new ModelAndView("statisticsView");
        if (sensorName == null || sensorName.isEmpty()) {
            mav.addObject("error", "Параметр sensorName не задан.");
            return mav;
        }
        Optional<Sensor> opt = sensorService.findSensorBySensorName(sensorName);
        if (opt.isEmpty()) {
            mav.addObject("error", "Датчик с именем " + sensorName + " не найден.");
            return mav;
        }
        if(!opt.get().getSensorStatus().equals(SensorStatus.GET_DATA)){
            mav.addObject("error", "Датчик с именем " + sensorName + " не принимает данные.");
            return mav;
        }
        return prepareStatisticsView(opt.get(), "day");
    }

    @PostMapping("/statistics")
    public ModelAndView filterStatistics(
            @RequestParam("sensorName") String sensorName,
            @RequestParam("period")     String period) {
        Optional<Sensor> opt = sensorService.findSensorBySensorName(sensorName);
        if (opt.isEmpty()) {
            ModelAndView mav = new ModelAndView("statisticsView");
            mav.addObject("error", "Сенсор с именем " + sensorName + " не найден.");
            return mav;
        }
        if(!opt.get().getSensorStatus().equals(SensorStatus.GET_DATA)){
            ModelAndView mav = new ModelAndView("statisticsView");
            mav.addObject("error", "Датчик с именем " + sensorName + " не принимает данные.");
            return mav;
        }
        return prepareStatisticsView(opt.get(), period);
    }

    private ModelAndView prepareStatisticsView(Sensor sensor, String period) {
        ModelAndView mav = new ModelAndView("statisticsView");

        ZoneId zone = ZoneId.of(sensor.getTimeZone());

        ZonedDateTime nowZ = ZonedDateTime.now(zone);
        ZonedDateTime startZ;
        switch (period.toLowerCase()) {
            case "week":  startZ = nowZ.minusWeeks(1);  break;
            case "month": startZ = nowZ.minusMonths(1); break;
            case "year":  startZ = nowZ.minusYears(1);  break;
            case "day":
            default:      startZ = nowZ.minusDays(1);   break;
        }

        LocalDateTime now    = nowZ.toLocalDateTime();
        LocalDateTime start  = startZ.toLocalDateTime();

        List<SensorData> data = sensorDataRepository
                .findBySensorAndLocalDateTimeBetween(sensor, start, now);
        if (data.isEmpty()) {
            mav.addObject("error", "Нет данных для сенсора "
                    + sensor.getSensorName() + " за выбранный период.");
            return mav;
        }

        double avgWL = data.stream().mapToDouble(SensorData::getWaterLevel).average().orElse(0);
        double maxWL = data.stream().mapToDouble(SensorData::getWaterLevel).max().orElse(0);
        double minWL = data.stream().mapToDouble(SensorData::getWaterLevel).min().orElse(0);

        double avgT = data.stream().filter(d -> d.getTemperature() != null)
                .mapToDouble(SensorData::getTemperature).average().orElse(0);
        double maxT = data.stream().filter(d -> d.getTemperature() != null)
                .mapToDouble(SensorData::getTemperature).max().orElse(0);
        double minT = data.stream().filter(d -> d.getTemperature() != null)
                .mapToDouble(SensorData::getTemperature).min().orElse(0);

        double avgH = data.stream().filter(d -> d.getHumidity() != null)
                .mapToDouble(SensorData::getHumidity).average().orElse(0);
        double maxH = data.stream().filter(d -> d.getHumidity() != null)
                .mapToDouble(SensorData::getHumidity).max().orElse(0);
        double minH = data.stream().filter(d -> d.getHumidity() != null)
                .mapToDouble(SensorData::getHumidity).min().orElse(0);

        mav.addObject("sensorData", data);
        mav.addObject("avgWaterLevel", avgWL);
        mav.addObject("maxWaterLevel", maxWL);
        mav.addObject("minWaterLevel", minWL);

        mav.addObject("avgTemperature", avgT);
        mav.addObject("maxTemperature", maxT);
        mav.addObject("minTemperature", minT);

        mav.addObject("avgHumidity", avgH);
        mav.addObject("maxHumidity", maxH);
        mav.addObject("minHumidity", minH);

        mav.addObject("sensorName", sensor.getSensorName());
        mav.addObject("period", period);

        String fmt = nowZ.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        mav.addObject("sensorCurrentTime", fmt);

        return mav;
    }
}

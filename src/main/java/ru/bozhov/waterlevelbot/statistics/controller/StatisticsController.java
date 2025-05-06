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
import java.util.*;
import java.util.stream.Collectors;

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
        ModelAndView mav = new ModelAndView("statisticsView");
        if (opt.isEmpty()) {
            mav.addObject("error", "Сенсор с именем " + sensorName + " не найден.");
            return mav;
        }
        if(!opt.get().getSensorStatus().equals(SensorStatus.GET_DATA)){
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

        // Вычисление медианных значений (float)
        List<Float> wlValues = data.stream()
                .map(SensorData::getWaterLevel)
                .sorted()
                .collect(Collectors.toList());
        float medianWL = computeMedian(wlValues);

        List<Float> tempValues = data.stream()
                .map(SensorData::getTemperature)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
        float medianT = computeMedian(tempValues);

        List<Float> humValues = data.stream()
                .map(SensorData::getHumidity)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
        float medianH = computeMedian(humValues);

        mav.addObject("sensorData", data);
        mav.addObject("medianWaterLevel", medianWL);
        mav.addObject("maxWaterLevel", wlValues.isEmpty() ? 0f : Collections.max(wlValues));
        mav.addObject("minWaterLevel", wlValues.isEmpty() ? 0f : Collections.min(wlValues));

        mav.addObject("medianTemperature", medianT);
        mav.addObject("maxTemperature", tempValues.isEmpty() ? 0f : Collections.max(tempValues));
        mav.addObject("minTemperature", tempValues.isEmpty() ? 0f : Collections.min(tempValues));

        mav.addObject("medianHumidity", medianH);
        mav.addObject("maxHumidity", humValues.isEmpty() ? 0f : Collections.max(humValues));
        mav.addObject("minHumidity", humValues.isEmpty() ? 0f : Collections.min(humValues));

        mav.addObject("sensorName", sensor.getSensorName());
        mav.addObject("period", period);

        String fmt = nowZ.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        mav.addObject("sensorCurrentTime", fmt);

        return mav;
    }

    private float computeMedian(List<Float> list) {
        if (list.isEmpty()) return 0f;
        int size = list.size();
        if (size % 2 == 1) {
            return list.get(size / 2);
        } else {
            return (list.get(size / 2 - 1) + list.get(size / 2)) / 2f;
        }
    }
}
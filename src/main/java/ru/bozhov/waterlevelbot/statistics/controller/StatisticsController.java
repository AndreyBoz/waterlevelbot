package ru.bozhov.waterlevelbot.statistics.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ModelAndView statistics(
            @RequestParam("sensorName") String sensorName,
            @RequestParam(value = "period", defaultValue = "day") String period) {

        // 1) Один раз создаём MAV и кладём в него параметр sensorName и period
        ModelAndView mav = new ModelAndView("statisticsView");
        mav.addObject("sensorName", sensorName);
        mav.addObject("period", period);

        // 2) Проверяем, что датчик существует и в статусе GET_DATA
        Optional<Sensor> opt = sensorService.findSensorBySensorName(sensorName);
        if (opt.isEmpty()) {
            mav.addObject("error", "Сенсор с именем «" + sensorName + "» не найден.");
            return mav;
        }
        Sensor sensor = opt.get();
        if (!SensorStatus.GET_DATA.equals(sensor.getSensorStatus())) {
            mav.addObject("error", "Сенсор «" + sensorName + "» не принимает данные.");
            return mav;
        }

        // 3) Дополняем остальными данными и возвращаем тот же MAV
        return prepareStatisticsView(mav, sensor, period);
    }

    private ModelAndView prepareStatisticsView(ModelAndView mav, Sensor sensor, String period) {
        ZoneId zone = ZoneId.of(sensor.getTimeZone());
        ZonedDateTime nowZ = ZonedDateTime.now(zone);

        // Определяем начало отрезка
        ZonedDateTime startZ;
        switch (period.toLowerCase()) {
            case "week":  startZ = nowZ.minusWeeks(1);  break;
            case "month": startZ = nowZ.minusMonths(1); break;
            case "year":  startZ = nowZ.minusYears(1);  break;
            default:      startZ = nowZ.minusDays(1);   break;
        }

        LocalDateTime start = startZ.toLocalDateTime();
        LocalDateTime now   = nowZ.toLocalDateTime();

        // Загружаем данные
        List<SensorData> data = sensorDataRepository
                .findBySensorAndLocalDateTimeBetween(sensor, start, now);

        if (data.isEmpty()) {
            mav.addObject("error",
                    "Нет данных для сенсора «" + sensor.getSensorName() + "» за выбранный период.");
            return mav;
        }

        // Считаем медианы и min/max
        List<Float> wl  = data.stream().map(SensorData::getWaterLevel).sorted().toList();
        List<Float> tmp = data.stream().map(SensorData::getTemperature)
                .filter(Objects::nonNull).sorted().toList();
        List<Float> hum = data.stream().map(SensorData::getHumidity)
                .filter(Objects::nonNull).sorted().toList();

        mav.addObject("sensorData",         data);
        mav.addObject("medianWaterLevel",   computeMedian(wl));
        mav.addObject("maxWaterLevel",      wl.isEmpty()  ? 0f : Collections.max(wl));
        mav.addObject("minWaterLevel",      wl.isEmpty()  ? 0f : Collections.min(wl));
        mav.addObject("medianTemperature",  computeMedian(tmp));
        mav.addObject("maxTemperature",     tmp.isEmpty() ? 0f : Collections.max(tmp));
        mav.addObject("minTemperature",     tmp.isEmpty() ? 0f : Collections.min(tmp));
        mav.addObject("medianHumidity",     computeMedian(hum));
        mav.addObject("maxHumidity",        hum.isEmpty() ? 0f : Collections.max(hum));
        mav.addObject("minHumidity",        hum.isEmpty() ? 0f : Collections.min(hum));

        // Текущее время датчика
        String fmt = nowZ.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        mav.addObject("sensorCurrentTime", fmt);

        return mav;
    }

    private float computeMedian(List<Float> list) {
        if (list.isEmpty()) return 0f;
        int n = list.size();
        return (n % 2 == 1)
                ? list.get(n/2)
                : (list.get(n/2 - 1) + list.get(n/2)) / 2f;
    }
}
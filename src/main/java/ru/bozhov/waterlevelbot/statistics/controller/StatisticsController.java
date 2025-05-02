package ru.bozhov.waterlevelbot.statistics.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorData;
import ru.bozhov.waterlevelbot.sensor.repository.SensorDataRepository;
import ru.bozhov.waterlevelbot.sensor.service.SensorService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Controller
@AllArgsConstructor
public class StatisticsController {

    private final SensorService sensorService;
    private final SensorDataRepository sensorDataRepository;

    /**
     * Отображает страницу статистики для выбранного сенсора с дефолтным периодом (day).
     * На странице будут кнопки для выбора другого периода.
     */
    @GetMapping("/statistics")
    public ModelAndView showStatisticsForm(@RequestParam(value = "sensorName", required = false) String sensorName) {
        ModelAndView mav = new ModelAndView("statisticsView");

        if (sensorName == null || sensorName.isEmpty()) {
            mav.addObject("error", "Параметр sensorName не задан.");
            return mav;
        }

        Optional<Sensor> sensorOptional = sensorService.findSensorBySensorName(sensorName);
        if (!sensorOptional.isPresent()) {
            mav.addObject("error", "Сенсор с именем " + sensorName + " не найден.");
            return mav;
        }

        // По умолчанию отображаем статистику за день
        return prepareStatisticsView(sensorOptional.get(), "day");
    }

    /**
     * Обрабатывает POST-запрос с выбранным периодом.
     * Форма на странице statisticsView.html отправляет sensorName и выбранный period.
     */
    @PostMapping("/statistics")
    public ModelAndView filterStatistics(
            @RequestParam("sensorName") String sensorName,
            @RequestParam("period") String period) {

        Optional<Sensor> sensorOptional = sensorService.findSensorBySensorName(sensorName);
        if (!sensorOptional.isPresent()) {
            ModelAndView mav = new ModelAndView("statisticsView");
            mav.addObject("error", "Сенсор с именем " + sensorName + " не найден.");
            return mav;
        }
        return prepareStatisticsView(sensorOptional.get(), period);
    }

    /**
     * Вспомогательный метод для подготовки данных и создания модели для отображения статистики.
     */
    private ModelAndView prepareStatisticsView(Sensor sensor, String period) {
        // Используйте имя представления, которое не конфликтует с URL, например "statisticsView"
        ModelAndView mav = new ModelAndView("statisticsView");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;

        switch (period.toLowerCase()) {
            case "week":
                start = now.minusWeeks(1);
                break;
            case "month":
                start = now.minusMonths(1);
                break;
            case "year":
                start = now.minusYears(1);
                break;
            case "day":
            default:
                start = now.minusDays(1);
        }

        List<SensorData> sensorData = sensorDataRepository.findBySensorAndLocalDateTimeBetween(sensor, start, now);
        if (sensorData.isEmpty()) {
            mav.addObject("error", "Нет данных для сенсора " + sensor.getSensorName() + " за выбранный период.");
            return mav;
        }

        // Агрегированные показатели для уровня воды
        double avgWaterLevel = sensorData.stream()
                .mapToDouble(SensorData::getWaterLevel)
                .average().orElse(0);
        double maxWaterLevel = sensorData.stream()
                .mapToDouble(SensorData::getWaterLevel)
                .max().orElse(0);
        double minWaterLevel = sensorData.stream()
                .mapToDouble(SensorData::getWaterLevel)
                .min().orElse(0);

        // Для температуры (учитывая, что поле может быть null)
        double avgTemperature = sensorData.stream()
                .filter(d -> d.getTemperature() != null)
                .mapToDouble(SensorData::getTemperature)
                .average().orElse(0);
        double maxTemperature = sensorData.stream()
                .filter(d -> d.getTemperature() != null)
                .mapToDouble(SensorData::getTemperature)
                .max().orElse(0);
        double minTemperature = sensorData.stream()
                .filter(d -> d.getTemperature() != null)
                .mapToDouble(SensorData::getTemperature)
                .min().orElse(0);

        // Для влажности
        double avgHumidity = sensorData.stream()
                .filter(d -> d.getHumidity() != null)
                .mapToDouble(SensorData::getHumidity)
                .average().orElse(0);
        double maxHumidity = sensorData.stream()
                .filter(d -> d.getHumidity() != null)
                .mapToDouble(SensorData::getHumidity)
                .max().orElse(0);
        double minHumidity = sensorData.stream()
                .filter(d -> d.getHumidity() != null)
                .mapToDouble(SensorData::getHumidity)
                .min().orElse(0);

        // Передаем данные в модель
        mav.addObject("sensorData", sensorData);
        mav.addObject("avgWaterLevel", avgWaterLevel);
        mav.addObject("maxWaterLevel", maxWaterLevel);
        mav.addObject("minWaterLevel", minWaterLevel);

        mav.addObject("avgTemperature", avgTemperature);
        mav.addObject("maxTemperature", maxTemperature);
        mav.addObject("minTemperature", minTemperature);

        mav.addObject("avgHumidity", avgHumidity);
        mav.addObject("maxHumidity", maxHumidity);
        mav.addObject("minHumidity", minHumidity);

        mav.addObject("sensorName", sensor.getSensorName());
        mav.addObject("period", period);
        return mav;
    }

}

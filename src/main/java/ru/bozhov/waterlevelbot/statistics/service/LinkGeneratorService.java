package ru.bozhov.waterlevelbot.statistics.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;


@Service
public class LinkGeneratorService {
    private static String STATISTICS_URL;

    @Value("${domain.url:https://localhost:8080/statistics}")
    private String statisticsUrl;

    @PostConstruct
    public void init() {
        STATISTICS_URL = statisticsUrl;
    }

    /**
     * Генерирует ссылку на страницу статистики с параметром sensorId для фильтрации данных.
     *
     * @param sensor сенсор
     * @return URL-адрес страницы статистики
     */
    public String generateStatisticsLink(Sensor sensor) {
        // Можно добавить дополнительную логику (например, шифрование параметров или временные метки)
        return STATISTICS_URL + "?sensorName=" + sensor.getSensorName();
    }
}

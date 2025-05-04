package ru.bozhov.waterlevelbot.yandex;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.bozhov.waterlevelbot.sensor.model.Coordinate;
import ru.bozhov.waterlevelbot.yandex.dto.GeocodeResponse;

import java.util.Locale;

@Slf4j
@Service
public class YandexApiService {

    @Value("${yandex.api.key}")
    private String apiKey;

    private static final String GEOCODER_URL =
            "https://geocode-maps.yandex.ru/1.x/?apikey=%s&geocode=%s,%s&results=1&format=json";

    private static final String MAP_LINK_TEMPLATE =
            "https://yandex.ru/maps/?ll=%f,%f&z=16&pt=%f,%f";

    public String getRegionByCoordinate(Coordinate coordinate) {
        String url = String.format(GEOCODER_URL,
                apiKey,
                coordinate.getLongitude(),
                coordinate.getLatitude());
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            GeocodeResponse geocode = new ObjectMapper()
                    .readValue(response.getBody(), GeocodeResponse.class);
            return geocode.getTitleAddress();
        } catch (Exception e) {
            log.error("Ошибка при геокодировании: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Возвращает публичную ссылку на Яндекс.Карты с маркером на заданной координате.
     *
     * @param coordinate широта/долгота
     * @return URL вида https://yandex.ru/maps/?ll=lon,lat&z=16&pt=lon,lat
     */
    public String getMapLink(Coordinate coordinate) {
        return String.format(Locale.US,
                MAP_LINK_TEMPLATE,
                coordinate.getLongitude(),  // долгота
                coordinate.getLatitude(),   // широта
                coordinate.getLongitude(),  // долгота (для pt)
                coordinate.getLatitude()    // широта (для pt)
        );
    }
}

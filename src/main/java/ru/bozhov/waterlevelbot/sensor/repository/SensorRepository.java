package ru.bozhov.waterlevelbot.sensor.repository;

import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorAddress;
import ru.bozhov.waterlevelbot.sensor.model.SensorStatus;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    @Query(value = "SELECT nextval('sensor_seq')", nativeQuery = true)
    long getNextSequence();

    @ReadOnlyProperty
    Optional<Sensor> findSensorBySensorName(String sensorName);

    @Query("SELECT s FROM Sensor s JOIN s.admins a WHERE a IN :admin AND s.sensorStatus = :status")
    Optional<Sensor> findSensorByAdminAndStatus(List<TelegramUser> admin, SensorStatus status);

    Sensor findSensorByAddress(SensorAddress address);

    @Query("SELECT s FROM Sensor s WHERE s.address is null ")
    List<Sensor> findByAddressIsNull();

    // Поиск сенсоров по региону
    List<Sensor> findByAddressRegion(String region);

    // Поиск сенсоров по региону и району
    List<Sensor> findByAddressRegionAndAddressLocalArea(String region, String localArea);

    // Поиск сенсоров по региону, району и типу водоёма
    List<Sensor> findByAddressRegionAndAddressLocalAreaAndAddressWaterFeatureType(String region, String localArea, String waterFeatureType);

    List<Sensor> findByAddressRegionAndAddressLocalAreaAndAddressWaterFeatureTypeAndAddressWaterFeatureName(
            String region, String localArea, String waterFeatureType, String waterFeatureName);

    List<Sensor> findByAddressRegionAndAddressLocalAreaAndAddressWaterFeatureTypeAndAddressWaterFeatureNameAndAddressNearestCity(
            String region, String localArea, String waterFeatureType, String waterFeatureName, String nearestCity);

    List<Sensor> findByAddressRegionAndAddressLocalAreaAndAddressWaterFeatureTypeAndAddressWaterFeatureNameAndAddressNearestCityAndAddressDescription(
            String region, String localArea, String waterFeatureType, String waterFeatureName, String nearestCity, String description);

    default List<Sensor> findByFilters(Map<String, String> filters) {
        return findAll().stream()
                .filter(sensor -> {
                    SensorAddress addr = sensor.getAddress();
                    if (addr == null) return false;
                    for (Map.Entry<String, String> entry : filters.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        switch (key) {
                            case "region" -> {
                                if (!value.equals(addr.getRegion())) return false;
                            }
                            case "localArea" -> {
                                if (!value.equals(addr.getLocalArea())) return false;
                            }
                            case "waterFeatureType" -> {
                                if (!value.equals(addr.getWaterFeatureType())) return false;
                            }
                            case "waterFeatureName" -> {
                                if (!value.equals(addr.getWaterFeatureName())) return false;
                            }
                            case "nearestCity" -> {
                                if (!value.equals(addr.getNearestCity())) return false;
                            }
                            case "description" -> {
                                if (!value.equals(addr.getDescription())) return false;
                            }
                            default -> { /* игнорировать неизвестные ключи */ }
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}

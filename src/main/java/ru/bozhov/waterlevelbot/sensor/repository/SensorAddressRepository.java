package ru.bozhov.waterlevelbot.sensor.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.bozhov.waterlevelbot.sensor.model.SensorAddress;

import java.util.List;


@Repository
public interface SensorAddressRepository extends JpaRepository<SensorAddress, Long> {

    @Query("FROM SensorAddress sa WHERE sa.region=(:region)")
    public SensorAddress findFirstByRegion(String region);

    @Query("FROM SensorAddress sa WHERE sa.region=(:region) AND sa.localArea=(:localArea)")
    public SensorAddress findFirstByRegionAndLocalArea(String region, String localArea);

    @Query("FROM SensorAddress sa WHERE sa.region=(:region)AND sa.localArea=(:localArea) AND sa.nearestCity=(:city)")
    public SensorAddress findFirstByRegionAndLocalAreaAndCity(String region, String localArea, String city);

    @Query("FROM SensorAddress sa " +
            "WHERE sa.region=(:region) " +
            "AND sa.localArea=(:localArea) " +
            "AND sa.nearestCity=(:city) " +
            "AND sa.waterFeatureType=(:waterFeatureType)")
    SensorAddress findFirstByRegionAndLocalAreaAndCityAndWaterFeatureType(String region, String localArea, String nearestCity, String waterFeatureType);

    @Query("FROM SensorAddress sa " +
            "WHERE sa.region=(:region) " +
            "AND sa.localArea=(:localArea) " +
            "AND sa.nearestCity=(:city) " +
            "AND sa.waterFeatureType=(:waterFeatureType)" +
            "AND sa.waterFeatureName=(:waterFeatureName)")
    SensorAddress findFirstByRegionAndLocalAreaAndCityAndWaterFeatureTypeAndWaterFeatureName(String region, String localArea, String nearestCity, String waterFeatureType, String waterFeatureName);

    @Query("SELECT sa.region FROM SensorAddress sa")
    public List<String> getAllRegions();

    @Query("SELECT (sa.localArea) FROM SensorAddress sa " +
            "WHERE sa.region = (:region)")
    public List<String> getAllAreas(String region);

    @Query("SELECT (sa.nearestCity) FROM SensorAddress sa " +
            "WHERE sa.region = (:region) " +
            "AND sa.localArea=(:localArea)")
    public List<String> getAllNearestCities(String region, String localArea);

    @Query("SELECT (sa.waterFeatureType) FROM SensorAddress sa " +
            "WHERE sa.region = (:region) " +
            "AND sa.localArea=(:localArea)" +
            "AND sa.nearestCity=(:nearestCity)")
    public List<String> getAllWaterFeatureTypes(String region, String localArea, String nearestCity);

    @Query("SELECT (sa.waterFeatureName) FROM SensorAddress sa " +
            "WHERE sa.region = (:region) " +
            "AND sa.localArea=(:localArea)" +
            "AND sa.nearestCity=(:nearestCity)" +
            "AND sa.waterFeatureType=(:waterFeatureType)")
    public List<String> getAllWaterFeatureNames(String region, String localArea, String nearestCity, String waterFeatureType);

    @Query("SELECT (sa.description) FROM SensorAddress sa " +
            "WHERE sa.region = (:region) " +
            "AND sa.localArea=(:localArea)" +
            "AND sa.nearestCity=(:nearestCity)" +
            "AND sa.waterFeatureType=(:waterFeatureType)" +
            "AND sa.waterFeatureName=(:waterFeatureName)")
    public List<String> getAllDescriptions(String region, String localArea, String nearestCity, String waterFeatureType, String waterFeatureName);

    @Query("FROM SensorAddress sa " +
            "WHERE sa.region = :region " +
            "AND sa.localArea = :localArea " +
            "AND sa.nearestCity = :nearestCity " +
            "AND sa.waterFeatureType = :waterFeatureType " +
            "AND sa.waterFeatureName = :waterFeatureName " +
            "AND sa.description = :description")
    SensorAddress findFirstSensorAddressByParam(String region, String localArea, String nearestCity, String waterFeatureType, String waterFeatureName, String description);

}

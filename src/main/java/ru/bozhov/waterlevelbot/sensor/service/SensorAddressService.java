package ru.bozhov.waterlevelbot.sensor.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorAddress;
import ru.bozhov.waterlevelbot.sensor.repository.SensorAddressRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class SensorAddressService {
    SensorAddressRepository repository;

    public SensorAddress findByRegion(String region){
        return repository.findFirstByRegion(region);
    }
    public List<String> findAllRegions() {
        return repository.getAllRegions();
    }


//    public List<String> findAllAreas(SensorSelectionDTO sensorSelectionDTO) {
//        return repository.getAllAreas(sensorSelectionDTO.getRegion());
//    }
//
//    public List<String> findAllCities(SensorSelectionDTO sensorSelectionDTO) {
//        return repository.getAllNearestCities(
//                sensorSelectionDTO.getRegion(),
//                sensorSelectionDTO.getLocalArea()
//        );
//    }
//
//    public List<String> findAllWaterFeatureTypes(SensorSelectionDTO sensorSelectionDTO) {
//        return repository.getAllWaterFeatureTypes(
//                sensorSelectionDTO.getRegion(),
//                sensorSelectionDTO.getLocalArea(),
//                sensorSelectionDTO.getNearestCity()
//        );
//    }
//
//    public List<String> findAllWaterFeatureNames(SensorSelectionDTO sensorSelectionDTO) {
//        return repository.getAllWaterFeatureNames(
//                sensorSelectionDTO.getRegion(),
//                sensorSelectionDTO.getLocalArea(),
//                sensorSelectionDTO.getNearestCity(),
//                sensorSelectionDTO.getWaterFeatureType()
//        );
//    }
//
//    public List<String> findAllDescriptions(SensorSelectionDTO sensorSelectionDTO) {
//        return repository.getAllDescriptions(
//                sensorSelectionDTO.getRegion(),
//                sensorSelectionDTO.getLocalArea(),
//                sensorSelectionDTO.getNearestCity(),
//                sensorSelectionDTO.getWaterFeatureType(),
//                sensorSelectionDTO.getWaterFeatureName()
//        );
//    }
//
//    public SensorAddress findAddressBySensorSelectionDTO(SensorSelectionDTO sensorSelectionDTO) {
//        return repository.findFirstSensorAddressByParam(
//                sensorSelectionDTO.getRegion(),
//                sensorSelectionDTO.getLocalArea(),
//                sensorSelectionDTO.getNearestCity(),
//                sensorSelectionDTO.getWaterFeatureType(),
//                sensorSelectionDTO.getWaterFeatureName(),
//                sensorSelectionDTO.getDescription()
//        );
//    }
//
//    public SensorAddress findByRegionAndArea(SensorSelectionDTO sensorSelectionDTO) {
//        return repository.findFirstByRegionAndLocalArea(
//                sensorSelectionDTO.getRegion(),
//                sensorSelectionDTO.getLocalArea()
//        );
//    }
//
//    public SensorAddress findByRegionAndAreaAndCity(SensorSelectionDTO sensorSelectionDTO) {
//        return repository.findFirstByRegionAndLocalAreaAndCity(
//                sensorSelectionDTO.getRegion(),
//                sensorSelectionDTO.getLocalArea(),
//                sensorSelectionDTO.getNearestCity()
//        );
//    }
//
//    public SensorAddress findByRegionAndAreaAndCityAndWaterFeatureType(SensorSelectionDTO sensorSelectionDTO) {
//        return repository.findFirstByRegionAndLocalAreaAndCityAndWaterFeatureType(
//                sensorSelectionDTO.getRegion(),
//                sensorSelectionDTO.getLocalArea(),
//                sensorSelectionDTO.getNearestCity(),
//                sensorSelectionDTO.getWaterFeatureType()
//        );
//    }
//
//    public SensorAddress findByRegionAndAreaAndCityAndWaterFeatureTypeAndWaterFeatureName(SensorSelectionDTO sensorSelectionDTO) {
//        return repository.findFirstByRegionAndLocalAreaAndCityAndWaterFeatureTypeAndWaterFeatureName(
//                sensorSelectionDTO.getRegion(),
//                sensorSelectionDTO.getLocalArea(),
//                sensorSelectionDTO.getNearestCity(),
//                sensorSelectionDTO.getWaterFeatureType(),
//                sensorSelectionDTO.getWaterFeatureName()
//        );
//    }
}

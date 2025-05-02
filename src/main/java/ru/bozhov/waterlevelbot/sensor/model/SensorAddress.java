package ru.bozhov.waterlevelbot.sensor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sensor_address")
public class SensorAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region")
    private String region;

    @Column(name = "local_area")
    private String localArea;

    @Column(name = "water_feature_type")
    private String waterFeatureType;

    @Column(name = "water_feature_name")
    private String waterFeatureName;

    @Column(name = "nearest_city")
    private String nearestCity;

    @Column(name = "description")
    private String description;

}

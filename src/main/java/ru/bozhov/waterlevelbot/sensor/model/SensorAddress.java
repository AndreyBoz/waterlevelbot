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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("🌊 *Информация о местоположении датчика* 🌊\n\n");

        if (waterFeatureName != null && !waterFeatureName.isEmpty()) {
            sb.append("📍 *Водный объект:* ")
                    .append(waterFeatureType != null ? waterFeatureType + " " : "")
                    .append("\"").append(waterFeatureName).append("\"\n");
        }

        if (localArea != null && !localArea.isEmpty()) {
            sb.append("🏘 *Район:* ").append(localArea);
            if (region != null && !region.isEmpty()) {
                sb.append(" (").append(region).append(")");
            }
            sb.append("\n");
        } else if (region != null && !region.isEmpty()) {
            sb.append("🗺 *Регион:* ").append(region).append("\n");
        }

        if (nearestCity != null && !nearestCity.isEmpty()) {
            sb.append("🏙 *Ближайший город:* ").append(nearestCity).append("\n");
        }

        if (description != null && !description.isEmpty()) {
            sb.append("\n📝 *Описание:* ").append(description).append("\n");
        }

        return sb.toString();
    }
}

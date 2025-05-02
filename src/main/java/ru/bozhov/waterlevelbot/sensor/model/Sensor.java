package ru.bozhov.waterlevelbot.sensor.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Getter
@Setter
@Entity
@Table(name = "sensor")
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sensor_seq")
    @SequenceGenerator(name = "sensor_seq", sequenceName = "sensor_seq", allocationSize = 1)
    private Long id;

    @Column(name = "sensor_name")
    private String sensorName;

    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "coordinate_id", referencedColumnName = "id")
    private Coordinate coordinate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", referencedColumnName = "id")
    private Set<TelegramUser> admins = new HashSet<>();

    @Enumerated
    @Column(name = "sensor_status")
    private SensorStatus sensorStatus = SensorStatus.AWAITING_REQUEST;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private SensorAddress address;

    @Column(name = "normal_level")
    private Float normalLevel;

    public Sensor(String sensorName) {
        this.sensorName = sensorName;
    }

    public Sensor() {

    }

    @Override
    public String toString() {
        String addressInfo = (address != null)
                ? String.format("\n\tРегион: %s,\n\tМестность: %s,\n\tТип объекта: %s,\n\tНазвание: %s,\n\tГород: %s,\n\tОписание: %s",
                address.getRegion(),
                address.getLocalArea(),
                address.getWaterFeatureType(),
                address.getWaterFeatureName(),
                address.getNearestCity(),
                address.getDescription())
                : "не указан";

        return String.format(
                "Датчик \"%s\" (ID: %d)%nКоординаты: %s%nАдрес: %s",
                sensorName,
                id,
                (coordinate != null ? coordinate.toString() : "не заданы"),
                addressInfo
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sensor sensor)) return false;
        return Objects.equals(getId(), sensor.getId()) && Objects.equals(getSensorName(), sensor.getSensorName()) && Objects.equals(getCoordinate(), sensor.getCoordinate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSensorName(), getCoordinate());
    }

    public void addAdmin(TelegramUser user) {
        if (user != null) {
            admins.add(user);
        }
    }
}

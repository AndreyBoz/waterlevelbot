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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Пользователи, подписанные на уведомления от датчика
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sensor_subscribers",
            joinColumns = @JoinColumn(name = "sensor_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<TelegramUser> subscribers = new HashSet<>();

    public Sensor() {
    }

    public Sensor(String sensorName) {
        this.sensorName = sensorName;
    }

    public void addAdmin(TelegramUser user) {
        if (user != null) {
            admins.add(user);
        }
    }

    public void addSubscriber(TelegramUser user) {
        if (user != null) {
            subscribers.add(user);
        }
    }

    public void removeSubscriber(TelegramUser user) {
        if (user != null) {
            subscribers.remove(user);
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Датчик \"%s\" (ID: %d)%n", sensorName, id));

        // Координаты
        String coordStr = (coordinate != null && Hibernate.isInitialized(coordinate))
                ? coordinate.toString()
                : "не заданы";
        sb.append("Координаты: ").append(coordStr).append("%n");

        // Адрес
        if (address != null && Hibernate.isInitialized(address)) {
            sb.append("Адрес:%n")
                    .append("\tРегион: ").append(address.getRegion()).append("%n")
                    .append("\tМестность: ").append(address.getLocalArea()).append("%n")
                    .append("\tТип объекта: ").append(address.getWaterFeatureType()).append("%n")
                    .append("\tНазвание: ").append(address.getWaterFeatureName()).append("%n")
                    .append("\tГород: ").append(address.getNearestCity()).append("%n")
                    .append("\tОписание: ").append(address.getDescription()).append("%n");
        } else {
            sb.append("Адрес: не указан%n");
        }

        // Нормальный уровень (всегда доступен, это простое поле)
        sb.append("Норм. уровень: ").append(normalLevel).append(" м%n");

        // Статус
        sb.append("Статус: ").append(sensorStatus).append("%n");

        // Администраторы
        String adminsInfo = Hibernate.isInitialized(admins)
                ? admins.stream().map(TelegramUser::getUserName).collect(Collectors.joining(", "))
                : "лениво";
        sb.append("Админы: ").append(adminsInfo).append("%n");

        // Подписчики
        String subsInfo = Hibernate.isInitialized(subscribers)
                ? subscribers.stream().map(TelegramUser::getUserName).collect(Collectors.joining(", "))
                : "лениво";
        sb.append("Подписчики: ").append(subsInfo).append("%n");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sensor sensor)) return false;
        return Objects.equals(getId(), sensor.getId())
                && Objects.equals(getSensorName(), sensor.getSensorName())
                && Objects.equals(getCoordinate(), sensor.getCoordinate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSensorName(), getCoordinate());
    }
}

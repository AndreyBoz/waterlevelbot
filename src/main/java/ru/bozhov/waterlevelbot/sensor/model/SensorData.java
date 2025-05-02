package ru.bozhov.waterlevelbot.sensor.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sensor_data")
public class SensorData implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "water_level", nullable = false)
    private Float waterLevel;

    @Column(name = "temperature")
    private Float temperature; // Новое поле: температура

    @Column(name = "humidity")
    private Float humidity; // Новое поле: влажность

    @ManyToOne
    @JoinColumn(name = "sensor_id", referencedColumnName = "id")
    @JsonIgnore
    private Sensor sensor;

    @Column(name = "time")
    private LocalDateTime localDateTime;
}
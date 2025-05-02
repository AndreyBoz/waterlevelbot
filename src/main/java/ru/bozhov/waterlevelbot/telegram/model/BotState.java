package ru.bozhov.waterlevelbot.telegram.model;

public enum BotState {
    IDLE,
    SENSOR_ID,
    STATISTICS,
    CURRENT_DATA,
    SENSOR_EDIT,
    SENSOR_EDIT_ACCEPT,
    REGION_SELECTION,
    DATE_RANGE,
    SENSOR_REGISTERED,
    DATA_READY,
    ALERT_SENT,
    ERROR
}

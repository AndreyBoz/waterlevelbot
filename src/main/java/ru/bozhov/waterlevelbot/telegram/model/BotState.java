package ru.bozhov.waterlevelbot.telegram.model;

public enum BotState {
    IDLE,
    SENSOR_ID,
    STATISTICS,
    CURRENT_DATA,
    EDIT_SENSOR_ADDRESS,
    SENSOR_EDIT_ACCEPT,
    SET_NORMAL_LEVEL,
    SET_NORMAL_LEVEL_ACCEPT,
    SUBSCRIBE_SENSOR,
    SET_GEOLOCATION,
    SET_GEOLOCATION_ACCEPT,
    VIEW_MAP
}

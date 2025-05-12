package ru.bozhov.waterlevelbot.telegram.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorData;
import ru.bozhov.waterlevelbot.telegram.config.WaterLevelBot;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class BotService {

    private final WaterLevelBot bot;

    public BotService(@Lazy WaterLevelBot bot) {
        this.bot = bot;
    }

    public void executeAnswerCallback(AnswerCallbackQuery answer) {
        try {
            bot.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Не удалось ответить на callback_query", e);
        }
    }

    public Boolean sendMessage(SendMessage message) {
        try {
            bot.execute(message);
            return true;
        } catch (TelegramApiException e) {
            log.error("Error execute message: {}", e.getMessage(), e);
            return false;
        }
    }

    public Boolean sendEditMessage(TelegramUser user, EditMessageText message) {
        try {
            bot.execute(message);
            return true;
        } catch (TelegramApiException e) {
            log.error("Error execute message: {}", e.getMessage(), e);
            return false;
        }
    }

    public void sendCriticalLevelMessage(Sensor sensor, Float level){
        try {
            for(var user: sensor.getSubscribers()){
                SendMessage message = new SendMessage();
                message.setChatId(user.getChatId());
                message.setText("Критический уровень воды зафиксирован на датчике: " + sensor.getSensorName() + "\n Тек. уровень: " + level + "\n" + sensor.getAddress()!=null ? sensor.getAddress().toString() : "");
                bot.execute(message);
            }
        } catch (TelegramApiException e) {
            log.error("Error execute message: {}", e.getMessage(), e);
        }
    }

    public void sendMessageForSubscribers(Sensor sensor, SensorData data){
        try {
            for(var user: sensor.getSubscribers()){
                SendMessage message = new SendMessage();
                message.setChatId(user.getChatId());
                message.setText(String.format("Датчик %s сделал замер:\n" +
                                "💧 Уровень воды: %.2f м\n" +
                                "🌡 Температура: %s°C\n" +
                                "💦 Влажность: %s%%\n" +
                                "⏰ Время измерения: %s\n" +
                                sensor.getAddress()!=null ? sensor.getAddress().toString(): "",
                        sensor.getSensorName(),
                        data.getWaterLevel(),
                        data.getTemperature() != null ? String.format("%.2f", data.getTemperature()) : "N/A",
                        data.getHumidity() != null ? String.format("%.2f", data.getHumidity()) : "N/A",
                        data.getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                );
                bot.execute(message);
            }
        } catch (TelegramApiException e) {
            log.error("Error execute message: {}", e.getMessage(), e);
        }
    }
}

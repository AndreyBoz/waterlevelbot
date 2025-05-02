package ru.bozhov.waterlevelbot.telegram.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.service.SensorAddressService;
import ru.bozhov.waterlevelbot.sensor.service.SensorService;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.utils.SendMessageUtils;

@Slf4j
@Service
public class TelegramService {

    TelegramUserService telegramUserService;

    SensorService sensorService;

    BotService botService;

    SensorAddressService sensorAddressService;

    private String HELLO_MESSAGE = "Добро пожаловать, %s.\nЭто телеграм бот, в котором вы сможете зарегистрировать свой сенсор, который будет отправлять нам текущий уровень воды в водном объекте, а также температуру и влажность.";

    public TelegramService(TelegramUserService telegramUserService, SensorService sensorService, BotService botService, SensorAddressService sensorAddressService) {
        this.telegramUserService = telegramUserService;
        this.sensorService = sensorService;
        this.botService = botService;
        this.sensorAddressService = sensorAddressService;
    }

    public SendMessage registerTelegramUser(Long chatId, String username){
        telegramUserService.registerNewUser(chatId, username);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(String.format(HELLO_MESSAGE, username));

        return SendMessageUtils.setStartMenuInline(sendMessage);
    }

    public String registerSensorCallback(TelegramUser user){
        if(user.getBotState().equals(BotState.SENSOR_ID.toString())){
            return null;
        }

        Sensor sensor = sensorService.createNewSensor(user);

        return sensor.getSensorName();
    }

}

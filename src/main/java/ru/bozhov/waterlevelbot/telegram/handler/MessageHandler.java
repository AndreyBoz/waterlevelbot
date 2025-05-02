package ru.bozhov.waterlevelbot.telegram.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorAddress;
import ru.bozhov.waterlevelbot.sensor.model.SensorStatus;
import ru.bozhov.waterlevelbot.sensor.repository.SensorRepository;
import ru.bozhov.waterlevelbot.sensor.service.SensorService;
import ru.bozhov.waterlevelbot.telegram.bot.util.SensorSelectionUtil;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;
import ru.bozhov.waterlevelbot.telegram.utils.SendMessageUtils;

import java.util.Optional;


@AllArgsConstructor
@Component
public class MessageHandler implements TelegramHandler {

    private TelegramService telegramService;

    private TelegramUserService telegramUserService;

    private SensorService sensorService;

    private SensorSelectionUtil selectionUtil;

    private BotService botService;


    @Override
    public Boolean matches(Update update) {
        return update.hasMessage() && update.getMessage().hasText() || update.hasMessage() && update.getMessage().hasLocation();
    }

    public SendMessage handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getUserName();
        Optional<TelegramUser> userOptional = telegramUserService.findUserByChatId(chatId);

        if(userOptional.isEmpty()){
            return telegramService.registerTelegramUser(chatId, username);
        }

        TelegramUser user = userOptional.get();

        if(user.getBotState().equals(BotState.SENSOR_EDIT_ACCEPT.toString())){
            String text = update.getMessage().getText();
            try {
                SensorAddress addr = parse(text);

                Sensor sensor = selectionUtil.getSelection(user.getChatId());
                sensor.setAddress(addr);
                sensor.setSensorStatus(SensorStatus.GET_DATA);
                sensorService.editSensorAddress(sensor, addr, user);


                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(user.getChatId()));
                message.setText("✅ Адрес сохранён: " + addr.toString());
                message.setReplyMarkup(SendMessageUtils.getBackKeyboard());
                botService.sendMessage(message);

                return null;
            } catch (IllegalArgumentException e) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(user.getChatId()));
                message.setText("❗ " + e.getMessage() + "\nПопробуйте ввести адрес ещё раз. Или можете вернуться назад в главное меню.");
                message.setReplyMarkup(SendMessageUtils.getBackKeyboard());

                botService.sendMessage(message);
            }

        }

        return errorMessage(update);

    }

    public SendMessage errorMessage(Update update){
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText("Ошибка, такой команды не существует.");

        return SendMessageUtils.setBackKeyboard(message);
    }

    public static SensorAddress parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Строка адреса не должна быть null");
        }
        // Разделяем по запятой, не более 6 частей (оставшаяся часть попадёт в описание)
        String[] parts = input.split("\\s*,\\s*", 6);
        if (parts.length < 6) {
            throw new IllegalArgumentException(
                    "Неверный формат адреса. Ожидается 6 частей, разделённых запятыми: " +
                            "Регион, Район, Тип водоёма, Название водоёма, Ближайший город, Описание"
            );
        }
        SensorAddress address = new SensorAddress();
        address.setRegion(parts[0].trim());
        address.setLocalArea(parts[1].trim());
        address.setWaterFeatureType(parts[2].trim());
        address.setWaterFeatureName(parts[3].trim());
        address.setNearestCity(parts[4].trim());
        address.setDescription(parts[5].trim());
        return address;
    }

}

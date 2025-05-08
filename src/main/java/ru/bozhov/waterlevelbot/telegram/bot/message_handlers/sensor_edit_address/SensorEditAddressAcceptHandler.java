package ru.bozhov.waterlevelbot.telegram.bot.message_handlers.sensor_edit_address;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorAddress;
import ru.bozhov.waterlevelbot.sensor.model.SensorStatus;
import ru.bozhov.waterlevelbot.sensor.service.SensorService;
import ru.bozhov.waterlevelbot.telegram.bot.message_handlers.BotStateMessageHandler;
import ru.bozhov.waterlevelbot.telegram.bot.util.SensorSelectionUtil;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;
import ru.bozhov.waterlevelbot.telegram.utils.SendMessageUtils;

@Component
@AllArgsConstructor
public class SensorEditAddressAcceptHandler implements BotStateMessageHandler {

    private final SensorService sensorService;
    private final SensorSelectionUtil selectionUtil;
    private final TelegramUserService telegramUserService;
    private final BotService botService;

    @Override
    public Boolean matches(TelegramUser user) {
        return BotState.SENSOR_EDIT_ACCEPT.name().equals(user.getBotState());
    }

    @Override
    public SendMessage handle(Update update, TelegramUser user) {
        String text = update.getMessage().getText();
        Long chatId = user.getChatId();

        try {
            SensorAddress addr = parse(text);
            Sensor sensor = selectionUtil.getSelection(chatId);
            sensor.setAddress(addr);
            sensor.setSensorStatus(SensorStatus.AWAITING_REQUEST);

            sensorService.editSensorAddress(sensor, addr, user);

            SendMessage msg = new SendMessage(chatId.toString(),
                    "✅ Адрес сохранён: " + addr);
            msg.setReplyMarkup(SendMessageUtils.getBackKeyboard());
            botService.sendMessage(msg);

        } catch (IllegalArgumentException ex) {
            SendMessage msg = new SendMessage(chatId.toString(),
                    "❗ Ошибка ввода: " + text +
                            "\nПопробуйте ввести адрес ещё раз или вернитесь в меню.");
            msg.setReplyMarkup(SendMessageUtils.getBackKeyboard());
            botService.sendMessage(msg);
        }
        return null;
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
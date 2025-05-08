package ru.bozhov.waterlevelbot.telegram.bot.message_handlers.geolocation_accpt;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.sensor.model.Coordinate;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.service.SensorService;
import ru.bozhov.waterlevelbot.telegram.bot.message_handlers.BotStateMessageHandler;
import ru.bozhov.waterlevelbot.telegram.bot.util.SensorSelectionUtil;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;
import ru.bozhov.waterlevelbot.telegram.utils.SendMessageUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor
public class GeolocationAcceptHandler implements BotStateMessageHandler {

    private final SensorService sensorService;
    private final SensorSelectionUtil selectionUtil;
    private final BotService botService;
    private final TelegramUserService telegramUserService;

    @Override
    public Boolean matches(TelegramUser user) {
        return BotState.SET_GEOLOCATION_ACCEPT.name().equals(user.getBotState());
    }

    @Override
    public SendMessage handle(Update update, TelegramUser user) {
        Long chatId = user.getChatId();
        try {
            Coordinate coord = extractCoordinateFromUpdate(update);
            Sensor sensor = selectionUtil.getSelection(chatId);
            sensorService.setCoordinate(sensor, coord);

            SendMessage msg = new SendMessage(chatId.toString(),
                    "✅ Геолокация сохранена: " + coord);
            msg.setReplyMarkup(SendMessageUtils.getBackKeyboard());
            botService.sendMessage(msg);

            // После успешного приёма вернём состояние в IDLE
            telegramUserService.changeBotState(user, BotState.IDLE);

        } catch (IllegalArgumentException ex) {
            SendMessage msg = new SendMessage(chatId.toString(),
                    "❗ Ошибка ввода: " + ex.getMessage() +
                            "\nПопробуйте отправить геолокацию ещё раз или вернитесь в меню.");
            msg.setReplyMarkup(SendMessageUtils.getBackKeyboard());
            botService.sendMessage(msg);
        }
        return null;
    }

    private Coordinate extractCoordinateFromUpdate(Update update) {
        if (update.getMessage().hasLocation()) {
            var location = update.getMessage().getLocation();
            return new Coordinate(location.getLatitude(), location.getLongitude());
        } else if (update.getMessage().hasText() && isCoordinate(update.getMessage().getText())) {
            return extractCoordinates(update.getMessage().getText())
                    .orElseThrow(() -> new IllegalArgumentException("Неверный формат координат"));
        }
        throw new IllegalArgumentException("Координаты отсутствуют в сообщении");
    }

    public Optional<Coordinate> extractCoordinates(String input) {
        // Регулярное выражение:
        // ^ - начало строки
        // ([+-]?\\d+(\\.\\d+)?) - первая группа: число с опциональным знаком и дробной частью (широта)
        // \\s+ - один или более пробелов
        // ([+-]?\\d+(\\.\\d+)?) - вторая группа: число с опциональным знаком и дробной частью (долгота)
        // $ - конец строки
        String regex = "^([+-]?\\d+(\\.\\d+)?)\\s+([+-]?\\d+(\\.\\d+)?)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            double latitude = Double.parseDouble(matcher.group(1));
            double longitude = Double.parseDouble(matcher.group(3));
            return Optional.of(new Coordinate(latitude, longitude));
        }
        return Optional.empty();
    }

    public boolean isCoordinate(String input) {
        // Регулярное выражение:
        // ^[+-]?\\d+(\\.\\d+)? – число с опциональным знаком и дробной частью
        // \\s+ – один или более пробелов
        // [+-]?\\d+(\\.\\d+)?$ – второе число с опциональным знаком и дробной частью до конца строки
        String regex = "^[+-]?\\d+(\\.\\d+)?\\s+[+-]?\\d+(\\.\\d+)?$";
        return input.matches(regex);
    }
}

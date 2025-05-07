package ru.bozhov.waterlevelbot.telegram.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.sensor.model.Coordinate;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorAddress;
import ru.bozhov.waterlevelbot.sensor.model.SensorStatus;
import ru.bozhov.waterlevelbot.sensor.service.SensorService;
import ru.bozhov.waterlevelbot.telegram.bot.util.SensorSelectionUtil;
import ru.bozhov.waterlevelbot.telegram.messages.CallBackMessages;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;
import ru.bozhov.waterlevelbot.telegram.utils.SendMessageUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

        if(update.getMessage().getText().equals("/start")){
            telegramUserService.changeBotState(user, BotState.IDLE);
            return CallBackMessages.getWelcomeMessage(chatId, user.getUserName());
        }

        if(user.getBotState().equals(BotState.SENSOR_EDIT_ACCEPT.toString())){
            String text = update.getMessage().getText();
            try {
                SensorAddress addr = parse(text);

                Sensor sensor = selectionUtil.getSelection(user.getChatId());
                sensor.setAddress(addr);
                sensor.setSensorStatus(SensorStatus.AWAITING_REQUEST);
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

        } else if(user.getBotState().equals(BotState.SET_NORMAL_LEVEL_ACCEPT.toString())){
            String text = update.getMessage().getText();
            try {
                Float normalLevel = Float.valueOf(text);

                Sensor sensor = selectionUtil.getSelection(user.getChatId());
                sensorService.setNormalLevel(sensor, normalLevel);



                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(user.getChatId()));
                message.setText("✅ Изменения сохранены: Нормальный уровень воды - " + normalLevel);
                message.setReplyMarkup(SendMessageUtils.getBackKeyboard());
                botService.sendMessage(message);

                return null;
            } catch (IllegalArgumentException e) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(user.getChatId()));
                message.setText("❗ " + e.getMessage() + "\nПопробуйте ввести уровень воды ещё раз. Или можете вернуться назад в главное меню.");
                message.setReplyMarkup(SendMessageUtils.getBackKeyboard());

                botService.sendMessage(message);
            }
        } else if (user.getBotState().equals(BotState.SET_GEOLOCATION_ACCEPT.toString())) {
            try {
                Coordinate coordinate = extractCoordinateFromUpdate(update);
                Sensor sensor = selectionUtil.getSelection(user.getChatId());
                sensorService.setCoordinate(sensor, coordinate);

                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText("✅ Геолокация сохранена: " + coordinate);
                message.setReplyMarkup(SendMessageUtils.getBackKeyboard());
                botService.sendMessage(message);
                telegramUserService.changeBotState(user, BotState.IDLE);

                return null;
            } catch (IllegalArgumentException e) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText("❗ " + e.getMessage() + "\nПопробуйте отправить геолокацию ещё раз. Или можете вернуться назад в главное меню.");
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

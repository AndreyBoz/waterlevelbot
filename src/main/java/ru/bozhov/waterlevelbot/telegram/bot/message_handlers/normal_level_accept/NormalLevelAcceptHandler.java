package ru.bozhov.waterlevelbot.telegram.bot.message_handlers.normal_level_accept;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.service.SensorService;
import ru.bozhov.waterlevelbot.telegram.bot.message_handlers.BotStateMessageHandler;
import ru.bozhov.waterlevelbot.telegram.bot.util.SensorSelectionUtil;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.utils.SendMessageUtils;

@Component
@AllArgsConstructor
public class NormalLevelAcceptHandler implements BotStateMessageHandler {

    private final SensorService sensorService;
    private final SensorSelectionUtil selectionUtil;
    private final BotService botService;

    @Override
    public Boolean matches(TelegramUser user) {
        return BotState.SET_NORMAL_LEVEL_ACCEPT.name().equals(user.getBotState());
    }

    @Override
    public SendMessage handle(Update update, TelegramUser user) {
        String text = update.getMessage().getText();
        Long chatId = user.getChatId();

        try {
            Float normalLevel = Float.valueOf(text);
            Sensor sensor = selectionUtil.getSelection(chatId);
            sensorService.setNormalLevel(sensor, normalLevel);

            SendMessage msg = new SendMessage(chatId.toString(),
                    "✅ Изменения сохранены: Нормальный уровень воды — " + normalLevel);
            msg.setReplyMarkup(SendMessageUtils.getBackKeyboard());
            botService.sendMessage(msg);

        } catch (IllegalArgumentException ex) {
            SendMessage msg = new SendMessage(chatId.toString(),
                    "❗ Ошибка ввода: "+ text +
                            "\nПопробуйте ввести уровень ещё раз или вернитесь в меню.");
            msg.setReplyMarkup(SendMessageUtils.getBackKeyboard());
            botService.sendMessage(msg);
        }
        return null;
    }
}
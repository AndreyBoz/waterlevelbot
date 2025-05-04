package ru.bozhov.waterlevelbot.telegram.bot.handlers.subscribe_sensor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.repository.SensorRepository;
import ru.bozhov.waterlevelbot.sensor.service.SensorService;
import ru.bozhov.waterlevelbot.statistics.service.LinkGeneratorService;
import ru.bozhov.waterlevelbot.telegram.bot.BotStateHandler;
import ru.bozhov.waterlevelbot.telegram.bot.util.SensorSelectionUtil;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;

import java.util.Collections;

@Slf4j
@Component
@AllArgsConstructor
public class SubscribeSensor implements BotStateHandler {
    private final BotService botService;
    private final SensorSelectionUtil selectionUtil;
    private final SensorService sensorService;
    private final TelegramUserService telegramUserService;
    private final LinkGeneratorService generatorService;

    @Override
    public Boolean matches(TelegramUser telegramUser) {
        return BotState.SUBSCRIBE_SENSOR.name().equals(telegramUser.getBotState());
    }

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        String callback = update.getCallbackQuery().getData();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        EditMessageText edit = selectionUtil.handleSelection(update, callback, messageId);
        if (edit != null) {
            botService.sendEditMessage(telegramUser, edit);
        }

        Sensor selected = selectionUtil.getSelection(telegramUser.getChatId());
        if (selected != null) {

            String prompt = String.format(
                    "✅ Вы успешно подписались на датчик \"%s\" (ID %d).",
                    selected.getSensorName(), selected.getId()
            );

            sensorService.subscribeSensor(telegramUser, selected);

            InlineKeyboardMarkup cancelMarkup = new InlineKeyboardMarkup(
                    Collections.singletonList(
                            Collections.singletonList(
                                    InlineKeyboardButton.builder()
                                            .text("Назад")
                                            .callbackData("GO_BACK")
                                            .build()
                            )
                    )
            );

            botService.sendEditMessage(telegramUser,
                    EditMessageText.builder()
                            .chatId(String.valueOf(telegramUser.getChatId()))
                            .messageId(messageId)
                            .text(prompt)
                            .replyMarkup(cancelMarkup)
                            .build()
            );

            telegramUserService.changeBotState(telegramUser, BotState.IDLE);
        }
    }
}

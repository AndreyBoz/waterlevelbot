package ru.bozhov.waterlevelbot.telegram.bot.handlers.setnormallevel;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.repository.SensorRepository;
import ru.bozhov.waterlevelbot.telegram.bot.BotStateHandler;
import ru.bozhov.waterlevelbot.telegram.bot.util.SensorSelectionUtil;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;

import java.util.Collections;

import static ru.bozhov.waterlevelbot.telegram.model.BotState.SET_NORMAL_LEVEL;

@Slf4j
@Component
@AllArgsConstructor
public class SetNormalLevelHandler implements BotStateHandler {
    private final BotService botService;
    private final SensorSelectionUtil selectionUtil;
    private final SensorRepository sensorRepo;
    private final TelegramUserService telegramUserService;

    @Override
    public Boolean matches(TelegramUser telegramUser) {
        return SET_NORMAL_LEVEL.name().equals(telegramUser.getBotState());
    }

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        String callback = update.getCallbackQuery().getData();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        // Обработка выбора датчика через селект
        EditMessageText edit = selectionUtil.handleSelection(update, callback, messageId);
        if (edit != null) {
            botService.sendEditMessage(telegramUser, edit);
        }

        Sensor selected = selectionUtil.getSelection(telegramUser.getChatId());
        if (selected != null) {
            // Запрашиваем нормальный уровень
            String prompt = String.format(
                    "✅ Выбран датчик \"%s\" (ID %d).\n\n" +
                            "📍 Введите нормальный уровень воды в метрах (например, 1.5):",
                    selected.getSensorName(), selected.getId()
            );

            telegramUserService.changeBotState(telegramUser, SET_NORMAL_LEVEL);
            // Кнопка отмены
            InlineKeyboardMarkup cancelMarkup = new InlineKeyboardMarkup(
                    Collections.singletonList(
                            Collections.singletonList(
                                    InlineKeyboardButton.builder()
                                            .text("Отмена")
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

            telegramUserService.changeBotState(telegramUser, BotState.SET_NORMAL_LEVEL_ACCEPT);
        }
    }
}

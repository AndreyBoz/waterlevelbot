package ru.bozhov.waterlevelbot.telegram.bot.callback_handlers.edit;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.repository.SensorRepository;
import ru.bozhov.waterlevelbot.telegram.bot.callback_handlers.BotStateCallbackHandler;
import ru.bozhov.waterlevelbot.telegram.bot.util.SensorSelectionUtil;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;

import java.util.Collections;

@Slf4j
@Component
@AllArgsConstructor
public class EditSensorCallbackHandler implements BotStateCallbackHandler {
    private final BotService botService;
    private final SensorSelectionUtil selectionUtil;
    private final SensorRepository sensorRepo;
    private final TelegramUserService telegramUserService;

    @Override
    public Boolean matches(TelegramUser telegramUser) {
        return BotState.EDIT_SENSOR_ADDRESS.name().equals(telegramUser.getBotState());
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
                    "✅ Выбран датчик \"%s\" (ID %d).\n\n" +
                            "📍 Пожалуйста, введите адрес датчика через запятую в формате:\n" +
                            "Регион, Район, Тип водоёма, Название водоёма, Ближайший город, Описание.\n\n" +
                            "📝 Пример:\n" +
                            "Московская область, Подмосковный район, Озеро, Сенеж, Солнечногорск, Живописное озеро недалеко от Москвы",
                    selected.getSensorName(), selected.getId()
            );

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

            telegramUserService.changeBotState(telegramUser, BotState.SENSOR_EDIT_ACCEPT);
        }
    }
}

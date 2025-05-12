package ru.bozhov.waterlevelbot.telegram.bot.callback_handlers.current_data;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorData;
import ru.bozhov.waterlevelbot.sensor.model.SensorStatus;
import ru.bozhov.waterlevelbot.sensor.service.SensorDataService;
import ru.bozhov.waterlevelbot.telegram.bot.callback_handlers.BotStateCallbackHandler;
import ru.bozhov.waterlevelbot.telegram.bot.util.SensorSelectionUtil;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;

import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Slf4j
@Component
@AllArgsConstructor
public class CurrentDataCallbackHandler implements BotStateCallbackHandler {
    private final BotService botService;
    private final SensorSelectionUtil selectionUtil;
    private final TelegramUserService telegramUserService;
    private final SensorDataService dataService;

    @Override
    public Boolean matches(TelegramUser telegramUser) {
        return BotState.CURRENT_DATA.name().equals(telegramUser.getBotState());
    }

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        String callback = update.getCallbackQuery().getData();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        // Обработка выбора датчика
        EditMessageText edit = selectionUtil.handleSelection(update, callback, messageId);
        if (edit != null) {
            botService.sendEditMessage(telegramUser, edit);
        }

        Sensor selected = selectionUtil.getSelection(telegramUser.getChatId());
        if (selected != null) {
            String prompt = "Датчик не готов к приёму данных.";
            if (selected.getSensorStatus().equals(SensorStatus.GET_DATA)){
                prompt = "Данных пока что нет.";
                SensorData data = dataService.getLastMeasure(selected);

                if(data!=null) {
                    prompt = String.format(
                            "✅ Последние данные для датчика \"%s\" (ID %d):\n" +
                                    "💧 Уровень воды: %.2f м\n" +
                                    "🌡 Температура: %s°C\n" +
                                    "💦 Влажность: %s%%\n" +
                                    "⏰ Время измерения: %s\n"+
                                    selected.getAddress()!=null ? selected.getAddress().toString() : "",
                            selected.getSensorName(), selected.getId(),
                            data.getWaterLevel(),
                            data.getTemperature() != null ? String.format("%.2f", data.getTemperature()) : "N/A",
                            data.getHumidity() != null ? String.format("%.2f", data.getHumidity()) : "N/A",
                            data.getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    );
                }
            }

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

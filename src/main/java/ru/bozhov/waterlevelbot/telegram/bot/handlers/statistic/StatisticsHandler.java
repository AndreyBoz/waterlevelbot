package ru.bozhov.waterlevelbot.telegram.bot.handlers.statistic;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorStatus;
import ru.bozhov.waterlevelbot.sensor.repository.SensorRepository;
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
public class StatisticsHandler implements BotStateHandler {
    private final BotService botService;
    private final SensorSelectionUtil selectionUtil;
    private final SensorRepository sensorRepo;
    private final TelegramUserService telegramUserService;
    private final LinkGeneratorService generatorService;

    @Override
    public Boolean matches(TelegramUser telegramUser) {
        return BotState.STATISTICS.name().equals(telegramUser.getBotState());
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
            String prompt = "Датчик не готов к приёму данных.";
            if (selected.getSensorStatus().equals(SensorStatus.GET_DATA)){
                String statsLink = generatorService.generateStatisticsLink(selected);

                prompt = String.format(
                        "✅ Выбран датчик \"%s\" (ID %d).\n" +
                                "📊 Графики доступны по ссылке: %s",
                        selected.getSensorName(), selected.getId(), statsLink
                );

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

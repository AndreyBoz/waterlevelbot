package ru.bozhov.waterlevelbot.telegram.bot.callback_handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;

import java.util.List;
import java.util.Vector;

@Slf4j
@Service
public class BotStateCallbackService {

    private final Vector<BotStateCallbackHandler> botStateCallbackHandlers;

    public BotStateCallbackService(List<BotStateCallbackHandler> handlers) {
        this.botStateCallbackHandlers = new Vector<>(handlers);
    }

    public void handleUpdateByBotState(Update update, TelegramUser telegramUser) {
        for (var handler : botStateCallbackHandlers) {
            if (handler.matches(telegramUser)) {
                try {
                    handler.handle(update, telegramUser);
                } catch (Exception e) {
                    log.error("Ошибка при обработке update {}: {}", update.getUpdateId(), e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }

    }

}

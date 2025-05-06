package ru.bozhov.waterlevelbot.telegram.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;

import java.util.List;
import java.util.Vector;

@Slf4j
@Service
public class BotStateService {

    private final Vector<BotStateHandler> botStateHandlers;

    public BotStateService(List<BotStateHandler> handlers) {
        this.botStateHandlers = new Vector<>(handlers);
    }

    public void handleUpdateByBotState(Update update, TelegramUser telegramUser) {
        if(update.getMessage()==null)
            return;

        for (var handler : botStateHandlers) {
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

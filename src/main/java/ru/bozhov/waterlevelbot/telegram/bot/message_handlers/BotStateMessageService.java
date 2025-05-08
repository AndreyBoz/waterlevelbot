package ru.bozhov.waterlevelbot.telegram.bot.message_handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.utils.SendMessageUtils;

import java.util.List;
import java.util.Vector;

@Slf4j
@Service
public class BotStateMessageService {
    private final Vector<BotStateMessageHandler> botStateMessageHandlers;

    public BotStateMessageService(List<BotStateMessageHandler> handlers) {
        this.botStateMessageHandlers = new Vector<>(handlers);
    }

    public SendMessage handleUpdateByBotState(Update update, TelegramUser telegramUser) {
        for (var handler : botStateMessageHandlers) {
            if (handler.matches(telegramUser)) {
                try {
                    return handler.handle(update, telegramUser);
                } catch (Exception e) {
                    log.error("Ошибка при обработке update {}: {}", update.getUpdateId(), e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }

        return errorMessage(update);
    }

    public SendMessage errorMessage(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText("Ошибка, такой команды не существует.");

        return SendMessageUtils.setBackKeyboard(message);
    }
}

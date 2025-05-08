package ru.bozhov.waterlevelbot.telegram.bot.callback_handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;

public interface BotStateCallbackHandler {
    Boolean matches(TelegramUser telegramUser);

    void handle(Update update, TelegramUser telegramUser);
}

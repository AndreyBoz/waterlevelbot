package ru.bozhov.waterlevelbot.telegram.bot;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;

public interface BotStateHandler {
    Boolean matches(TelegramUser telegramUser);

    void handle(Update update, TelegramUser telegramUser);
}

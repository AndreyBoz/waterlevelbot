package ru.bozhov.waterlevelbot.telegram.bot.message_handlers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;

public interface BotStateMessageHandler {
    Boolean matches(TelegramUser telegramUser);

    SendMessage handle(Update update, TelegramUser telegramUser);
}

package ru.bozhov.waterlevelbot.telegram.handler;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramHandler {

    Boolean matches(Update update);

}

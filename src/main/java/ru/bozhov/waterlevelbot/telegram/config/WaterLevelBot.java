package ru.bozhov.waterlevelbot.telegram.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.handler.CallbackHandler;
import ru.bozhov.waterlevelbot.telegram.handler.MessageHandler;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaterLevelBot extends TelegramWebhookBot {

    private final String botPath;
    private final String botUsername;
    private final String botToken;

    MessageHandler messageHandler;

    CallbackHandler callbackHandler;

    public WaterLevelBot(TelegramProperties properties, MessageHandler messageHandler, CallbackHandler callbackHandler) {
        this.botPath = properties.getBotPath();
        this.botToken = properties.getBotToken();
        this.botUsername = properties.getBotName();
        this.messageHandler = messageHandler;
        this.callbackHandler = callbackHandler;
    }


    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (callbackHandler.matches(update)) {
            callbackHandler.handle(update);
            return null;
        }

        return messageHandler.handle(update);

    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


}
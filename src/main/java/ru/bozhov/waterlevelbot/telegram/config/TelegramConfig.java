package ru.bozhov.waterlevelbot.telegram.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.bozhov.waterlevelbot.telegram.handler.CallbackHandler;
import ru.bozhov.waterlevelbot.telegram.handler.MessageHandler;
import ru.bozhov.waterlevelbot.telegram.messages.CallBackMessages;

@Slf4j
@Configuration
@AllArgsConstructor
public class TelegramConfig {
    private final TelegramProperties telegramProperties;

    @Bean
    public WaterLevelBot springWebhookBot(MessageHandler messageHandler, CallbackHandler callbackHandler) {
        WaterLevelBot bot = new WaterLevelBot(telegramProperties, messageHandler, callbackHandler);

        return bot;
    }

}

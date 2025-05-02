package ru.bozhov.waterlevelbot.telegram.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.config.WaterLevelBot;

@RestController
@AllArgsConstructor
public class TelegramController {

    private final WaterLevelBot bot;

    @Qualifier("webhookObjectMapper")
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/webhook", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleRequest(@RequestBody String body) throws JsonProcessingException {
        Update update = objectMapper.readValue(body, Update.class);
        System.out.println("Получен запрос: " + update);
        BotApiMethod<?> response = bot.onWebhookUpdateReceived(update);
        String jsonResponse = objectMapper.writeValueAsString(response);
        return ResponseEntity.ok(jsonResponse);
    }
}


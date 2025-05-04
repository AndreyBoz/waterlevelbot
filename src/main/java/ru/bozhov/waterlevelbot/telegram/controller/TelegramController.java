package ru.bozhov.waterlevelbot.telegram.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.config.WaterLevelBot;

@Slf4j
@RestController
@AllArgsConstructor
public class TelegramController {

    private final WaterLevelBot bot;

    @Qualifier("webhookObjectMapper")
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/webhook", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleRequest(@RequestBody String body) throws JsonProcessingException {
        Update update = objectMapper.readValue(body, Update.class);

        ObjectWriter nonNullWriter = objectMapper.copy()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writer();

        String logJson = nonNullWriter.writeValueAsString(update);
        log.info("Получен запрос: {}", logJson);
        BotApiMethod<?> response = bot.onWebhookUpdateReceived(update);
        String jsonResponse = objectMapper.writeValueAsString(response);
        return ResponseEntity.ok(jsonResponse);
    }
}


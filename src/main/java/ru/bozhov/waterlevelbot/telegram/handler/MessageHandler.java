package ru.bozhov.waterlevelbot.telegram.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.bot.message_handlers.BotStateMessageService;
import ru.bozhov.waterlevelbot.telegram.messages.CallBackMessages;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.TelegramService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;

import java.util.Optional;


@Component
@AllArgsConstructor
public class MessageHandler implements TelegramHandler {

    private final TelegramService telegramService;
    private final TelegramUserService telegramUserService;
    private final BotStateMessageService botStateMessageService;

    @Override
    public Boolean matches(Update update) {
        return update.hasMessage() &&
                (update.getMessage().hasText() || update.getMessage().hasLocation());
    }

    public SendMessage handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getUserName();

        Optional<TelegramUser> userOpt = telegramUserService.findUserByChatId(chatId);
        if (userOpt.isEmpty()) {
            return telegramService.registerTelegramUser(chatId, username);
        }

        TelegramUser user = userOpt.get();

        if(update.getMessage().getText().equals("/start")){
            telegramUserService.changeBotState(user, BotState.IDLE);
            return CallBackMessages.getWelcomeMessage(chatId, user.getUserName());
        }

        return botStateMessageService.handleUpdateByBotState(update, user);
    }
}
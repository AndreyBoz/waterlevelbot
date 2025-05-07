package ru.bozhov.waterlevelbot.telegram.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.bozhov.waterlevelbot.telegram.bot.BotStateService;
import ru.bozhov.waterlevelbot.telegram.bot.util.SensorSelectionUtil;
import ru.bozhov.waterlevelbot.telegram.messages.CallBackMessages;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.service.BotService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramService;
import ru.bozhov.waterlevelbot.telegram.service.TelegramUserService;
import ru.bozhov.waterlevelbot.telegram.utils.SendMessageUtils;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CallbackHandler implements TelegramHandler {

    TelegramService telegramService;

    TelegramUserService telegramUserService;

    BotService botService;

    BotStateService botStateService;

    SensorSelectionUtil selectionUtil;

    @Override
    public Boolean matches(Update update) {
        return update.hasCallbackQuery();
    }

    public void handle(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Optional<TelegramUser> userOptional = telegramUserService.findUserByChatId(chatId);

        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId())
                .build();
        botService.executeAnswerCallback(answer);

        if (userOptional.isEmpty()) {
            botService.sendMessage(telegramService.registerTelegramUser(chatId, update.getCallbackQuery().getFrom().getUserName()));
        }

        TelegramUser user = userOptional.get();

        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        if (callbackData.equals("GO_BACK")) {
            botService.sendEditMessage(user, CallBackMessages.getWelcomeMessage(chatId, messageId, user.getUserName()));
            telegramUserService.changeBotState(user, BotState.IDLE);
            return;
        }

        if (user.getBotState().equals(BotState.IDLE.toString())) {
            String newText = null;
            switch (callbackData) {
                case "REGISTER_SENSOR":
                    botService.sendEditMessage(user, CallBackMessages.getRegisterSensorMessage(chatId, messageId, telegramService.registerSensorCallback(user)));
                    return;
                case "SHOW_STATS":
                    selectionUtil.clearState(chatId);
                    telegramUserService.changeBotState(user, BotState.STATISTICS);
                    break;
                case "EDIT_SENSOR_ADDRESS":
                    selectionUtil.clearState(chatId);
                    telegramUserService.changeBotState(user, BotState.EDIT_SENSOR_ADDRESS);
                    break;
                case "SET_NORMAL_LEVEL":
                    selectionUtil.clearState(chatId);
                    telegramUserService.changeBotState(user, BotState.SET_NORMAL_LEVEL);
                    break;
                case "SUBSCRIBE_SENSOR":
                    selectionUtil.clearState(chatId);
                    telegramUserService.changeBotState(user, BotState.SUBSCRIBE_SENSOR);
                    break;
                case "SET_GEOLOCATION":
                    selectionUtil.clearState(chatId);
                    telegramUserService.changeBotState(user, BotState.SET_GEOLOCATION);
                    break;
                case "VIEW_MAP":
                    selectionUtil.clearState(chatId);
                    telegramUserService.changeBotState(user, BotState.VIEW_MAP);
                    break;
                case "SHOW_HELP":
                    botService.sendEditMessage(user, CallBackMessages.getHelpMessage(chatId, messageId));
                    return;
                case "SHOW_DATA":
                    selectionUtil.clearState(chatId);
                    telegramUserService.changeBotState(user, BotState.CURRENT_DATA);
                    break;
                default:
                    telegramUserService.changeBotState(user, BotState.IDLE);
                    botService.sendMessage(errorMessage(update));
                    return;
            }
        }

        botStateService.handleUpdateByBotState(update, user);
    }

    public SendMessage errorMessage(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        message.setText("Ошибка, такой команды не существует.");

        return SendMessageUtils.setBackKeyboard(message);
    }

}

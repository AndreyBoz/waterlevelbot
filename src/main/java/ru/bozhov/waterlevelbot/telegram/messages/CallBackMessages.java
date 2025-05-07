package ru.bozhov.waterlevelbot.telegram.messages;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.bozhov.waterlevelbot.telegram.utils.SendMessageUtils;

@UtilityClass
public class CallBackMessages {
    private String HELP_MESSAGE = "Это бот для мониторинга уровня воды. Вы можете зарегистрировать датчик и получать уведомления.";

    private String HELLO_MESSAGE = "Добро пожаловать, %s.\nЭто телеграм бот, в котором вы сможете зарегистрировать свой сенсор, который будет отправлять нам текущий уровень воды в водном объекте, а также температуру и влажность.";

    private String REGISTER_MESSAGE = "Ваше уникальное имя сенсора: %s;\nВставьте его в скетч, и запустите регистрацию.";

    public EditMessageText getHelpMessage(Long chatId, int messageId){
        return EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .text(HELP_MESSAGE)
                .replyMarkup(SendMessageUtils.getBackKeyboard())
                .build();
    }

    public EditMessageText getWelcomeEditMessage(Long chatId, int messageId, String username){
        return EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .text(String.format(HELLO_MESSAGE, username))
                .replyMarkup(SendMessageUtils.getStartMenuInline())
                .build();
    }

    public SendMessage getWelcomeMessage(Long chatId, String username){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(String.format(HELLO_MESSAGE, username));
        return message;
    }

    public EditMessageText getRegisterSensorMessage(Long chatId, int messageId, String sensorName){
        return EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .text(String.format(REGISTER_MESSAGE, sensorName))
                .replyMarkup(SendMessageUtils.getBackKeyboard())
                .build();
    }
}

package ru.bozhov.waterlevelbot.telegram.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class SendMessageUtils {

    public SendMessage setStartMenuInline(SendMessage message) {
        message.setReplyMarkup(getStartMenuInline());
        return message;
    }

    public SendMessage setBackKeyboard(SendMessage message) {
        message.setReplyMarkup(getBackKeyboard());
        return message;
    }

    public InlineKeyboardMarkup getStartMenuInline() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();

        InlineKeyboardButton btnRegister = InlineKeyboardButton.builder()
                .text("Зарегистрировать датчик")
                .callbackData("REGISTER_SENSOR")
                .build();
        InlineKeyboardButton btnStats = InlineKeyboardButton.builder()
                .text("Статистика")
                .callbackData("SHOW_STATS")
                .build();
        List<InlineKeyboardButton> row1 = Arrays.asList(btnRegister, btnStats);

        InlineKeyboardButton btnConfigure = InlineKeyboardButton.builder()
                .text("Настроить сенсор")
                .callbackData("CONFIGURE_SENSOR")
                .build();
        InlineKeyboardButton btnHelp = InlineKeyboardButton.builder()
                .text("Помощь")
                .callbackData("SHOW_HELP")
                .build();
        List<InlineKeyboardButton> row2 = Arrays.asList(btnConfigure, btnHelp);

        InlineKeyboardButton btnData = InlineKeyboardButton.builder()
                .text("Данные")
                .callbackData("SHOW_DATA")
                .build();
        List<InlineKeyboardButton> row3 = Collections.singletonList(btnData);

        inlineKeyboard.setKeyboard(Arrays.asList(row1, row2, row3));

        return inlineKeyboard;
    }

    public InlineKeyboardMarkup getBackKeyboard() {
        InlineKeyboardButton btnBack = InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("GO_BACK")
                .build();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(Collections.singletonList(
                Collections.singletonList(btnBack)
        ));
        return markup;
    }
}

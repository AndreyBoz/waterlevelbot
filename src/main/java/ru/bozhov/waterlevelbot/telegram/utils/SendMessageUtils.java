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

        // 1-я строка: регистрация и статистика
        InlineKeyboardButton btnRegister = InlineKeyboardButton.builder()
                .text("Зарегистрировать датчик")
                .callbackData("REGISTER_SENSOR")
                .build();
        InlineKeyboardButton btnStats = InlineKeyboardButton.builder()
                .text("Статистика")
                .callbackData("SHOW_STATS")
                .build();
        List<InlineKeyboardButton> row1 = Arrays.asList(btnRegister, btnStats);

        // 2-я строка: настройка и помощь
        InlineKeyboardButton btnConfigure = InlineKeyboardButton.builder()
                .text("Настроить датчик")
                .callbackData("EDIT_SENSOR_ADDRESS")
                .build();
        InlineKeyboardButton btnHelp = InlineKeyboardButton.builder()
                .text("Помощь")
                .callbackData("SHOW_HELP")
                .build();
        List<InlineKeyboardButton> row2 = Arrays.asList(btnConfigure, btnHelp);

        // 3-я строка: показ данных и установка норм. уровня
        InlineKeyboardButton btnData = InlineKeyboardButton.builder()
                .text("Данные")
                .callbackData("SHOW_DATA")
                .build();
        InlineKeyboardButton btnSetLevel = InlineKeyboardButton.builder()
                .text("Установить уровень воды")
                .callbackData("SET_NORMAL_LEVEL")
                .build();
        List<InlineKeyboardButton> row3 = Arrays.asList(btnData, btnSetLevel);

        // 4-я строка: подписка на уведомления
        InlineKeyboardButton btnSubscribe = InlineKeyboardButton.builder()
                .text("Подписаться на датчик")
                .callbackData("SUBSCRIBE_SENSOR")
                .build();
        List<InlineKeyboardButton> row4 = Collections.singletonList(btnSubscribe);

        // 5-я строка: геолокация и просмотр на карте
        InlineKeyboardButton btnSetLocation = InlineKeyboardButton.builder()
                .text("Установить геолокацию")
                .callbackData("SET_GEOLOCATION")
                .build();
        InlineKeyboardButton btnViewMap = InlineKeyboardButton.builder()
                .text("Посмотреть на карте")
                .callbackData("VIEW_MAP")
                .build();
        List<InlineKeyboardButton> row5 = Arrays.asList(btnSetLocation, btnViewMap);

        inlineKeyboard.setKeyboard(Arrays.asList(row1, row2, row3, row4, row5));
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

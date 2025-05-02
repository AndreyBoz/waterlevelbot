package ru.bozhov.waterlevelbot.telegram.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bozhov.waterlevelbot.telegram.model.BotState;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;
import ru.bozhov.waterlevelbot.telegram.model.UserType;
import ru.bozhov.waterlevelbot.telegram.repository.TelegramUserRepository;

import java.util.Optional;


@AllArgsConstructor
@Service
public class TelegramUserService {

    TelegramUserRepository telegramUserRepository;

    public TelegramUser registerNewUser(Long chatId, String username){
        TelegramUser user = createUser(chatId, username);

        return telegramUserRepository.save(user);
    }


    private static TelegramUser createUser(Long chatId, String username) {
        TelegramUser user = new TelegramUser();

        user.setChatId(chatId);
        user.setType(UserType.USER);
        user.setBotState(BotState.IDLE.toString());
        user.setUserName(username);

        return user;
    }

    public void changeBotState(TelegramUser user, BotState botState){
        user.setBotState(botState.toString());
        telegramUserRepository.saveAndFlush(user);
    }

    public Optional<TelegramUser> findUserByChatId(Long chatId) {
        return telegramUserRepository.findById(chatId);
    }
}

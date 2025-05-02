package ru.bozhov.waterlevelbot.telegram.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.bozhov.waterlevelbot.telegram.model.TelegramUser;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
}

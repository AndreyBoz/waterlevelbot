package ru.bozhov.waterlevelbot.telegram.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "telegram_user")
public class TelegramUser {

    @Id
    private Long chatId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "bot_state")
    private String botState;

    @Column(name = "user_type")
    @Enumerated(value = EnumType.STRING)
    private UserType type;

}

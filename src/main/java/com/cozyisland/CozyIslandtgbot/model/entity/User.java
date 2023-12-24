package com.cozyisland.CozyIslandtgbot.model.entity;

import com.cozyisland.CozyIslandtgbot.enums.UserState;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "UserTable")
public class User {
    @Id
    private long chatId;
    private String firstName;
    private String userName = "не указано";
    private String phoneNumber;
    private int menuMessageId;
    private int currentListIndex;
    @Enumerated(EnumType.STRING)
    private UserState state;
}

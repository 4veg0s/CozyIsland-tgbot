package com.cozyisland.CozyIslandtgbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

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
    private int menuMessageId;
}

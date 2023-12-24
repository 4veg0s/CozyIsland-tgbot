package com.cozyisland.CozyIslandtgbot.model.entity;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class FeedbackPK implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Long chatId;
}

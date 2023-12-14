package com.cozyisland.CozyIslandtgbot.model.entity;


import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "feedback")
public class Feedback {
    @Id
    private FeedbackPK pk;
    @Lob
    private String feedbackText;
    private int rate;
}

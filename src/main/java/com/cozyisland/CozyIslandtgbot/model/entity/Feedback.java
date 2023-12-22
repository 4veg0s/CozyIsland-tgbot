package com.cozyisland.CozyIslandtgbot.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

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
    @CreationTimestamp
    private Timestamp appliedAt;
    private String status = "на рассмотрении";
}

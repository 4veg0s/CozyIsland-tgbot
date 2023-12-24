package com.cozyisland.CozyIslandtgbot.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
    @Column(length = 400)
    private String feedbackText;
    private int rate;
    @CreationTimestamp
    private Timestamp appliedAt;
    private String status = "на рассмотрении";
}

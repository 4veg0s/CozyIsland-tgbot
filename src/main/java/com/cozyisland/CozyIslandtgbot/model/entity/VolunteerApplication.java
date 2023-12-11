package com.cozyisland.CozyIslandtgbot.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "volunteerApplication")
public class VolunteerApplication {
    @Id
    private long chatId;
    private String firstname;
    private String userName = "не указано";
    private String phoneNumber;
    @CreationTimestamp
    private Timestamp appliedAt;
    private String status = "на рассмотрении";
}

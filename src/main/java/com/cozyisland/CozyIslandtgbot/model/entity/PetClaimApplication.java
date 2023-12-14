package com.cozyisland.CozyIslandtgbot.model.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "petClaimApplication")
public class PetClaimApplication {
    @EmbeddedId
    private PetClaimApplicationPK pk;
    @CreationTimestamp
    private Timestamp appliedAt;
    private String status = "на рассмотрении";
    private Timestamp visitDate;
    private int notificationMessageId;
}

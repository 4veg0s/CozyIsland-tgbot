package com.cozyisland.CozyIslandtgbot.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class PetClaimApplicationPK implements Serializable {
    @Column(length = 255000)
    private String id;
    private long chatId;
}

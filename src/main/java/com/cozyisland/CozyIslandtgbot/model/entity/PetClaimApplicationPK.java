package com.cozyisland.CozyIslandtgbot.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
public class PetClaimApplicationPK implements Serializable {
    @Column(length = 255000)
    private String id;
    private long chatId;
}

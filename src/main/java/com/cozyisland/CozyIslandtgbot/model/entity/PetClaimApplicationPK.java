package com.cozyisland.CozyIslandtgbot.model.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
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

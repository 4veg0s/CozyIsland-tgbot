package com.cozyisland.CozyIslandtgbot.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "petImages")
public class PetImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;
    private String telegramFileId;
    @OneToOne
    private BinaryContent binaryContent;
}

package com.cozyisland.CozyIslandtgbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.web.WebProperties;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "petTable")
public class Pet {

    @Column(length = 255000)
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String category = "не указана";
    private String name = "не указано";
    private String age = "не указан";
    private boolean sterilized;
    @Column(length = 250000)
    private String about = "не указан";
    private Long imageId;
}

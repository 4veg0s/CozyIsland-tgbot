package com.cozyisland.CozyIslandtgbot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity(name = "petTable")
public class Pet {

    @Column(length = 255000)
    @jakarta.persistence.Id
    private String id;
    private String name = "Не указано";
    private String age = "Не указан";
    private boolean sterilized;
    @Column(length = 250000)
    private String about;
    private Long imageId;
}

package com.cozyisland.CozyIslandtgbot.model.entity;

import lombok.*;

import javax.persistence.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "petTable")
public class Pet {

    @Column(length = 255000)
    @Id
    private String id;
    private String category = "не указана";
    private String name = "не указано";
    private String age = "не указан";
    private boolean sterilized;
    @Column(length = 250000)
    private String about = "не указан";
    private String imageId;
}

package com.cozyisland.CozyIslandtgbot.controller;

import com.cozyisland.CozyIslandtgbot.model.entity.Pet;
import com.cozyisland.CozyIslandtgbot.model.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/getPets")
public class PetsController {
    @Autowired
    PetRepository petRepository;
    @CrossOrigin(origins = "*")
    @GetMapping
    public ResponseEntity<List<Pet>> getPets() {
        try {
            List<Pet> petList = new ArrayList<>(StreamSupport
                    .stream(petRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList()));
            return new ResponseEntity<>(petList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

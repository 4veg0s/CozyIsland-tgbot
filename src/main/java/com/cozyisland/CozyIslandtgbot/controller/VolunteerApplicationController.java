package com.cozyisland.CozyIslandtgbot.controller;

import com.cozyisland.CozyIslandtgbot.model.entity.VolunteerApplication;
import com.cozyisland.CozyIslandtgbot.model.repository.VolunteerApplicationRepository;
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
@RequestMapping("/getVolunteerApplication")
public class VolunteerApplicationController {
    @Autowired
    VolunteerApplicationRepository volunteerApplicationRepository;

    @CrossOrigin(origins = "*")
    @GetMapping
    public ResponseEntity<List<VolunteerApplication>> getVolunteerApplication() {
        try {
            List<VolunteerApplication> volunteerApplicationList = new ArrayList<>(StreamSupport
                    .stream(volunteerApplicationRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList()));
            return new ResponseEntity<>(volunteerApplicationList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
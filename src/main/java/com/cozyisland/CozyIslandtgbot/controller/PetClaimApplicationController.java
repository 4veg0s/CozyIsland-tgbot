package com.cozyisland.CozyIslandtgbot.controller;

import com.cozyisland.CozyIslandtgbot.model.entity.PetClaimApplication;
import com.cozyisland.CozyIslandtgbot.model.repository.PetClaimApplicationRepository;
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
@RequestMapping("/getPetClaimApplication")
public class PetClaimApplicationController {
    @Autowired
    PetClaimApplicationRepository petClaimApplicationRepository;
    @Autowired
    PetRepository petRepository;

    @CrossOrigin(origins = "*")
    @GetMapping
    public ResponseEntity<List<PetClaimApplication>> getPetClaimApplication() {
        try {
            List<PetClaimApplication> petClaimApplicationList = new ArrayList<>(StreamSupport
                    .stream(petClaimApplicationRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList()));
            return new ResponseEntity<>(petClaimApplicationList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
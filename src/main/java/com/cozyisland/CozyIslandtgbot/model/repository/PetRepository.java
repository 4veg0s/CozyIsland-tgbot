package com.cozyisland.CozyIslandtgbot.model.repository;

import com.cozyisland.CozyIslandtgbot.model.entity.Pet;
import org.springframework.data.repository.CrudRepository;

public interface PetRepository extends CrudRepository<Pet, String> {
}

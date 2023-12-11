package com.cozyisland.CozyIslandtgbot.model.repository;

import com.cozyisland.CozyIslandtgbot.model.entity.PetClaimApplication;
import com.cozyisland.CozyIslandtgbot.model.entity.PetClaimApplicationPK;
import org.springframework.data.repository.CrudRepository;

public interface PetClaimApplicationRepository extends CrudRepository<PetClaimApplication, PetClaimApplicationPK> {
}

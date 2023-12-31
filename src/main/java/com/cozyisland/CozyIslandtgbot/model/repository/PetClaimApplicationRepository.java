package com.cozyisland.CozyIslandtgbot.model.repository;

import com.cozyisland.CozyIslandtgbot.model.entity.PetClaimApplication;
import com.cozyisland.CozyIslandtgbot.model.entity.PetClaimApplicationPK;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PetClaimApplicationRepository extends CrudRepository<PetClaimApplication, PetClaimApplicationPK> {
    List<PetClaimApplication> findByPkChatId(long chatId);
}

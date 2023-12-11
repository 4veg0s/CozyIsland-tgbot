package com.cozyisland.CozyIslandtgbot.model.repository;

import com.cozyisland.CozyIslandtgbot.model.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}

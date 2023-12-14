package com.cozyisland.CozyIslandtgbot.model.repository;

import com.cozyisland.CozyIslandtgbot.model.entity.Feedback;
import com.cozyisland.CozyIslandtgbot.model.entity.FeedbackPK;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FeedbackRepository extends CrudRepository<Feedback, FeedbackPK> {
    List<Feedback> findByPkChatId(long chatId);
}

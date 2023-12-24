package com.cozyisland.CozyIslandtgbot.model.repository;

import com.cozyisland.CozyIslandtgbot.model.entity.Feedback;
import com.cozyisland.CozyIslandtgbot.model.entity.FeedbackPK;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends CrudRepository<Feedback, FeedbackPK> {
    List<Feedback> findByPkChatId(long chatId);
    @Query(value = "SELECT * FROM feedback WHERE status = 'на рассмотрении'", nativeQuery = true)
    List<Feedback> findByStatusToApprove();
    @Query(value = "SELECT * FROM feedback WHERE status like 'одобрен%'", nativeQuery = true)
    List<Feedback> findByStatusApproved();
    @Query(value = "SELECT * FROM feedback WHERE chat_id = ?1 AND status = 'на рассмотрении'", nativeQuery = true)
    List<Feedback> findByChatIdAndStatusToApprove(long chatId);
}

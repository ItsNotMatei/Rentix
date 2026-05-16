package com.example.demo.repository;

import com.example.demo.model.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    List<DirectMessage> findByConversation_IdOrderByCreatedAtAsc(Long conversationId);

    long countByConversation_IdAndSeenFalseAndSenderIdNot(Long conversationId, Long userId);

    @Modifying
    @Query("""
        UPDATE DirectMessage m SET m.seen = true
        WHERE m.conversation.id = :conversationId
        AND m.senderId <> :userId
        AND m.seen = false
        """)
    int markAsSeen(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}

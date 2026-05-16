package com.example.demo.repository;

import com.example.demo.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
        SELECT c FROM Conversation c
        WHERE (c.participantOneId = :userId OR c.participantTwoId = :userId)
        ORDER BY c.updatedAt DESC
        """)
    List<Conversation> findByParticipant(@Param("userId") Long userId);

    @Query("""
        SELECT c FROM Conversation c
        WHERE c.listingId = :listingId
        AND ((c.participantOneId = :u1 AND c.participantTwoId = :u2)
          OR (c.participantOneId = :u2 AND c.participantTwoId = :u1))
        """)
    Optional<Conversation> findByListingAndParticipants(
            @Param("listingId") Long listingId,
            @Param("u1") Long u1,
            @Param("u2") Long u2
    );
}

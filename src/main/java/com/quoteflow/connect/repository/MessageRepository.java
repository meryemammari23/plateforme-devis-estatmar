package com.quoteflow.connect.repository;

import com.quoteflow.connect.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByDateCreationAsc(Long conversationId);

    Page<Message> findByConversationIdOrderByDateCreationDesc(Long conversationId, Pageable pageable);

    long countByConversationId(Long conversationId);

    long countByConversationIdAndTypeNot(Long conversationId,
                                         com.quoteflow.connect.entity.MessageType type);

    @Query("""
            select count(m) from Message m
            where m.conversation.id = :conversationId
              and m.expediteur.id <> :userId
              and m.lu = false
            """)
    long countUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Modifying
    @Query("""
            update Message m set m.lu = true
            where m.conversation.id = :conversationId
              and m.expediteur.id <> :userId
              and m.lu = false
            """)
    int markConversationRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Query("""
            select m from Message m
            where m.conversation.id = :conversationId
              and m.expediteur.id <> :userId
              and m.lu = false
            """)
    List<Message> findUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}


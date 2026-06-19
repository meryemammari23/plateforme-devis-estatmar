package com.quoteflow.connect.repository;

import com.quoteflow.connect.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {

    @Query("""
            select count(a) from MessageAttachment a
            where a.message.conversation.id = :conversationId
            """)
    long countByConversationId(@Param("conversationId") Long conversationId);
}


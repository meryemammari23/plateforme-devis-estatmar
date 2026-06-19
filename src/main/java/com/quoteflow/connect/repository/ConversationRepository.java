package com.quoteflow.connect.repository;

import com.quoteflow.connect.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByDemandeId(Long demandeId);

    /**
     * Conversations visibles par un utilisateur : celles dont il est le client
     * ou l'employÃ© attribuÃ©. (L'ADMIN voit tout via findAll, gÃ©rÃ© au service.)
     * Adapter les chemins 'demande.client.id' / 'demande.employe.id' Ã  votre modÃ¨le.
     */
    @Query("""
            select c from Conversation c
            where c.demande.client.id = :userId
               or c.demande.employe.id = :userId
            order by c.dateDernierMessage desc nulls last, c.dateCreation desc
            """)
    List<Conversation> findAllForUser(@Param("userId") Long userId);
}


package com.quoteflow.connect.entity;

import com.quoteflow.entity.User; // <-- adapter au package rÃ©el
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "message", indexes = {
        @Index(name = "idx_message_conversation", columnList = "conversation_id"),
        @Index(name = "idx_message_date", columnList = "dateCreation")
})
@Getter
@Setter
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expediteur_id", nullable = false)
    private User expediteur;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    /** Flag dÃ©normalisÃ© : message lu par le destinataire (pour compteurs rapides). */
    @Column(nullable = false)
    private boolean lu = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType type = MessageType.TEXTE;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageAttachment> attachments = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (dateCreation == null) dateCreation = Instant.now();
    }

    public void addAttachment(MessageAttachment a) {
        attachments.add(a);
        a.setMessage(this);
    }
}


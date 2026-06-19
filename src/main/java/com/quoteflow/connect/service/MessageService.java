package com.quoteflow.connect.service;

import com.quoteflow.connect.dto.MessageDTO;
import com.quoteflow.connect.dto.RealtimeNotification;
import com.quoteflow.connect.entity.*;
import com.quoteflow.connect.mapper.ConnectMapper;
import com.quoteflow.connect.repository.*;
import com.quoteflow.entity.DemandeDevis; // <-- adapter
import com.quoteflow.entity.User;          // <-- adapter
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepo;
    private final ReadReceiptRepository receiptRepo;
    private final ConversationService conversationService;
    private final FileStorageService storage;
    private final RealtimeNotificationService realtime;

    public MessageService(MessageRepository messageRepo,
                          ReadReceiptRepository receiptRepo,
                          ConversationService conversationService,
                          FileStorageService storage,
                          RealtimeNotificationService realtime) {
        this.messageRepo = messageRepo;
        this.receiptRepo = receiptRepo;
        this.conversationService = conversationService;
        this.storage = storage;
        this.realtime = realtime;
    }

    /** Envoi d'un message (texte + piÃ¨ces jointes optionnelles). */
    @Transactional
    public MessageDTO send(Long conversationId, User expediteur,
                           String contenu, List<MultipartFile> fichiers) {

        Conversation conv = conversationService.loadAuthorized(conversationId, expediteur);

        boolean aFichiers = fichiers != null && !fichiers.isEmpty();
        boolean aTexte = contenu != null && !contenu.isBlank();
        if (!aTexte && !aFichiers)
            throw new IllegalArgumentException("Message vide");

        Message m = new Message();
        m.setConversation(conv);
        m.setExpediteur(expediteur);
        m.setContenu(aTexte ? contenu.trim() : null);
        m.setType(aFichiers ? MessageType.FICHIER : MessageType.TEXTE);

        if (aFichiers) {
            for (MultipartFile f : fichiers) {
                if (f == null || f.isEmpty()) continue;
                FileStorageService.StoredFile stored = storage.store(f, "messages");
                MessageAttachment a = new MessageAttachment();
                a.setNomFichier(stored.nomOriginal());
                a.setChemin(stored.chemin());
                a.setTypeMime(stored.typeMime());
                a.setTaille(stored.taille());
                m.addAttachment(a);
            }
        }

        Message saved = messageRepo.save(m);
        conv.setDateDernierMessage(saved.getDateCreation());

        MessageDTO dto = ConnectMapper.toMessageDTO(saved);

        // Temps rÃ©el : diffusion dans la conversation + notification au destinataire
        realtime.broadcastMessage(conversationId, dto);
        notifyRecipient(conv, expediteur, dto, aFichiers);

        return dto;
    }

    /** Marque comme lus tous les messages reÃ§us dans la conversation. */
    @Transactional
    public int markAsRead(Long conversationId, User lecteur) {
        conversationService.loadAuthorized(conversationId, lecteur);
        List<Message> nonLus = messageRepo.findUnread(conversationId, lecteur.getId());
        for (Message m : nonLus) {
            if (!receiptRepo.existsByMessageIdAndUtilisateurId(m.getId(), lecteur.getId())) {
                ReadReceipt r = new ReadReceipt();
                r.setMessage(m);
                r.setUtilisateur(lecteur);
                r.setDateLecture(Instant.now());
                receiptRepo.save(r);
            }
        }
        int updated = messageRepo.markConversationRead(conversationId, lecteur.getId());
        if (updated > 0) realtime.broadcastRead(conversationId, lecteur.getId());
        return updated;
    }

    private void notifyRecipient(Conversation conv, User expediteur, MessageDTO dto, boolean fichier) {
        DemandeDevis d = conv.getDemande();
        List<User> participants = new ArrayList<>();
        if (d.getClient() != null) participants.add(d.getClient());
        if (d.getEmploye() != null) participants.add(d.getEmploye());

        for (User p : participants) {
            if (p.getId().equals(expediteur.getId())) continue; // pas Ã  soi-mÃªme
            realtime.notifyUser(p.getId(), RealtimeNotification.of(
                    fichier ? "NOUVEAU_DOCUMENT" : "NOUVEAU_MESSAGE",
                    conv.getId(), d.getId(),
                    ConnectMapper.nomComplet(expediteur),
                    dto.contenu() != null ? dto.contenu() : "a envoyÃ© un document"
            ));
        }
    }
}


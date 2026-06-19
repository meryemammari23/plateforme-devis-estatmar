package com.quoteflow.connect.service;

import com.quoteflow.connect.dto.*;
import com.quoteflow.connect.entity.Conversation;
import com.quoteflow.connect.entity.Message;
import com.quoteflow.connect.entity.MessageType;
import com.quoteflow.connect.mapper.ConnectMapper;
import com.quoteflow.connect.repository.*;
import com.quoteflow.entity.DemandeDevis; // <-- adapter
import com.quoteflow.entity.User;          // <-- adapter
import com.quoteflow.repository.DemandeDevisRepository; // <-- adapter
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final MessageAttachmentRepository attachmentRepo;
    private final ActiviteRepository activiteRepo;
    private final DemandeDevisRepository demandeRepo;

    public ConversationService(ConversationRepository conversationRepo,
                               MessageRepository messageRepo,
                               MessageAttachmentRepository attachmentRepo,
                               ActiviteRepository activiteRepo,
                               DemandeDevisRepository demandeRepo) {
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.attachmentRepo = attachmentRepo;
        this.activiteRepo = activiteRepo;
        this.demandeRepo = demandeRepo;
    }

    /** RÃ©cupÃ¨re (ou crÃ©e Ã  la volÃ©e) l'espace collaboratif d'une demande. */
    @Transactional
    public Conversation getOrCreate(Long demandeId, User current) {
        DemandeDevis demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + demandeId));
        checkAccessToDemande(demande, current);
        return conversationRepo.findByDemandeId(demandeId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setDemande(demande);
                    return conversationRepo.save(c);
                });
    }

    /** Liste des espaces visibles par l'utilisateur (ADMIN voit tout). */
    @Transactional(readOnly = true)
    public List<ConversationSummaryDTO> listForUser(User current) {
        List<Conversation> conversations = isAdmin(current)
                ? conversationRepo.findAll()
                : conversationRepo.findAllForUser(current.getId());

        return conversations.stream().map(c -> {
            DemandeDevis d = c.getDemande();
            long nonLus = messageRepo.countUnread(c.getId(), current.getId());
            String dernier = messageRepo
                    .findByConversationIdOrderByDateCreationDesc(c.getId(),
                            org.springframework.data.domain.PageRequest.of(0, 1))
                    .stream().findFirst().map(Message::getContenu).orElse(null);
            return new ConversationSummaryDTO(
                    c.getId(),
                    d.getId(),
                    safeRef(d),
                    safeStatut(d),
                    apercu(dernier),
                    c.getDateDernierMessage(),
                    nonLus
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public ConversationDetailDTO getDetail(Long conversationId, User current) {
        Conversation c = loadAuthorized(conversationId, current);
        DemandeDevis d = c.getDemande();

        List<MessageDTO> messages = messageRepo
                .findByConversationIdOrderByDateCreationAsc(conversationId)
                .stream().map(ConnectMapper::toMessageDTO).toList();

        List<ActiviteDTO> activites = activiteRepo
                .findByDemandeIdOrderByDateCreationAsc(d.getId())
                .stream().map(ConnectMapper::toActiviteDTO).toList();

        return new ConversationDetailDTO(
                c.getId(), d.getId(), safeRef(d), safeDescription(d), safeStatut(d),
                c.getDateCreation(), messages, activites
        );
    }

    @Transactional(readOnly = true)
    public ConversationStatsDTO stats(Long conversationId, User current) {
        loadAuthorized(conversationId, current);
        long nbMessages = messageRepo.countByConversationId(conversationId);
        long nbFichiers = attachmentRepo.countByConversationId(conversationId);

        List<Message> ordered = messageRepo.findByConversationIdOrderByDateCreationAsc(conversationId);
        Long tempsMoyen = tempsMoyenReponse(ordered);
        Instant derniere = ordered.isEmpty() ? null
                : ordered.get(ordered.size() - 1).getDateCreation();

        return new ConversationStatsDTO(nbMessages, nbFichiers, tempsMoyen, derniere);
    }

    /** Temps moyen entre un message et la premiÃ¨re rÃ©ponse d'un autre expÃ©diteur. */
    private Long tempsMoyenReponse(List<Message> ordered) {
        long total = 0; int paires = 0;
        for (int i = 1; i < ordered.size(); i++) {
            Message prev = ordered.get(i - 1), cur = ordered.get(i);
            boolean reponse = prev.getExpediteur() != null && cur.getExpediteur() != null
                    && !prev.getExpediteur().getId().equals(cur.getExpediteur().getId());
            if (reponse) {
                total += Duration.between(prev.getDateCreation(), cur.getDateCreation()).getSeconds();
                paires++;
            }
        }
        return paires == 0 ? null : total / paires;
    }

    // ---------- AccÃ¨s / sÃ©curitÃ© mÃ©tier ----------

    /** Charge la conversation en vÃ©rifiant que l'utilisateur y a droit. */
    public Conversation loadAuthorized(Long conversationId, User current) {
        Conversation c = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation introuvable"));
        checkAccessToDemande(c.getDemande(), current);
        return c;
    }

    private void checkAccessToDemande(DemandeDevis d, User current) {
        if (isAdmin(current)) return;
        Long uid = current.getId();
        boolean estClient = d.getClient() != null && uid.equals(d.getClient().getId());
        boolean estEmploye = d.getEmploye() != null && uid.equals(d.getEmploye().getId());
        if (!estClient && !estEmploye)
            throw new AccessDeniedException("AccÃ¨s non autorisÃ© Ã  cet espace");
    }

    private boolean isAdmin(User u) {
        return u.getRole() != null && "ADMIN".equals(u.getRole().toString());
    }

    // ---------- Helpers tolÃ©rants au modÃ¨le existant ----------
    private String safeRef(DemandeDevis d)        { try { return d.getReference(); }   catch (Exception e) { return "QF-" + d.getId(); } }
    private String safeStatut(DemandeDevis d)     { try { return String.valueOf(d.getStatut()); } catch (Exception e) { return null; } }
    private String safeDescription(DemandeDevis d){ try { return d.getDescription(); } catch (Exception e) { return null; } }
    private String apercu(String s) {
        if (s == null) return null;
        return s.length() > 80 ? s.substring(0, 80) + "â€¦" : s;
    }
}


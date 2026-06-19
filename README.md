# QuoteFlow — Module Meryem (harnais de test local)

Mini-projet **Spring Boot autonome** pour tester **tes tâches** en local, sans rien
installer d'autre : la base est **H2 en mémoire**, des **données de démo** sont créées
au démarrage, et **Swagger** te permet de tout tester sans frontend.

Ce qu'il couvre (ton périmètre) :
- Demandes de devis (création, suivi, attribution) → module Client
- Upload de pièces jointes (le « trou fonctionnel » du cahier des charges)
- **Récap PDF de la demande** (destiné à l'employé)
- **PDF du devis** chiffré (lignes, totaux) — désormais ta tâche
- Intégration **n8n** : les 3 webhooks branchés aux bons événements

---

## 1. Prérequis

- **Java 17** (`java -version` doit afficher 17)
- **Maven** (`mvn -version`) — ou utilise le wrapper si tu en ajoutes un

Rien d'autre. Pas de MySQL, pas de config.

## 2. Lancer

```bash
cd quoteflow-meryem
mvn spring-boot:run
```

Au démarrage, la console affiche les ids de démo. Ensuite ouvre :

- **Swagger (pour tester)** : http://localhost:8080/swagger-ui.html
- **Console H2 (voir la base)** : http://localhost:8080/h2-console
  (JDBC URL : `jdbc:h2:mem:quoteflow`, user `sa`, pas de mot de passe)

> Le projet tourne **même si n8n n'est pas lancé** : les appels webhook échouent
> en silence (log `[n8n] Webhook KO`) sans casser le reste. Lance ton n8n
> (`n8n start`, port 5678) pour voir les emails partir réellement.

## 3. Scénario de test complet (via Swagger ou curl)

**a) Créer une demande**
```bash
curl -X POST http://localhost:8080/api/demandes \
  -H "Content-Type: application/json" \
  -d '{"description":"Application mobile de gestion","budget":20000}'
```
→ statut `EN_ATTENTE`, webhook `confirmation-demande` déclenché.

**b) Ajouter une pièce jointe** (remplace 1 par l'id de ta demande)
```bash
curl -X POST http://localhost:8080/api/demandes/1/pieces-jointes \
  -F "fichier=@/chemin/vers/un_fichier.pdf"
```

**c) Télécharger le récap PDF de la demande** (pour l'employé)
```bash
curl -OJ http://localhost:8080/api/demandes/1/recap-pdf
```

**d) Attribuer à un employé**
```bash
curl -X PUT "http://localhost:8080/api/demandes/1/attribuer"
```
→ statut `EN_COURS`, webhook `changement-statut` déclenché.

**e) Créer le devis** (génère le PDF + passe la demande à `VALIDE`)
```bash
curl -X POST http://localhost:8080/api/devis \
  -H "Content-Type: application/json" \
  -d '{
        "demandeId": 1,
        "commentaire": "Devis valable 30 jours",
        "lignes": [
          {"designation":"Developpement back-end","quantite":10,"prixUnitaire":500},
          {"designation":"Developpement front-end","quantite":8,"prixUnitaire":450}
        ]
      }'
```
→ webhooks `envoi-devis` (avec `pdfUrl`) + `changement-statut: VALIDE`.

**f) Télécharger le PDF du devis**
```bash
curl -OJ http://localhost:8080/api/devis/1/pdf
```

Les PDF générés sont aussi sauvegardés dans `./generated/`.

---

## 4. Ce qui est volontairement simplifié

C'est un **harnais de test de ton module**, pas le projet d'équipe intégré :

- **Pas de sécurité JWT** : tu testes tes endpoints sans token. Dans le projet
  final, c'est la couche de Douae (`@PreAuthorize`, `JwtAuthFilter`) qui protège
  tout. → à réintégrer au merge.
- **Pas de frontend React** : tu testes via Swagger/curl. Ton interface client
  React s'ajoute par-dessus ces endpoints.
- **H2 en mémoire** : la base est remise à zéro à chaque arrêt. Le projet
  d'équipe utilise MySQL.
- **`User` simplifié** : version minimale pour les FK ; l'entité réelle vient
  du module auth.

## 5. Pistes pour ta revue (ajouter / modifier / améliorer / supprimer)

- Le **template du PDF devis** (logo, TVA, conditions, devise) → `PdfService`
- Faut-il un **endpoint de liste/téléchargement des pièces jointes** ?
- Le **récap demande** : in-app (actuel) ou push email à l'employé (= 4e workflow n8n à voir avec Ibtihal)
- Gérer le **refus** d'une demande (`REFUSE` + webhook) — non implémenté ici
- La **devise** est codée en « MAD » → à externaliser si besoin

## 6. Arborescence

```
src/main/java/com/quoteflow/
├── entity/      User, DemandeDevis, Devis, LigneDevis, PieceJointe, enums
├── repository/  4 repositories JPA
├── dto/         DemandeRequest, DevisRequest, LigneDevisRequest
├── service/     DemandeService, DevisService, PieceJointeService,
│                DemandePdfService, PdfService, N8nWebhookService
├── controller/  DemandeController, DevisController, PieceJointeController
├── config/      DataSeeder (donnees demo), OpenApiConfig
└── exception/   ResourceNotFoundException, GlobalExceptionHandler
```

# QuoteFlow — Plateforme de gestion de devis (Estatmar)

Application web permettant à un **client** de soumettre une demande de devis, à un
**employé** de la traiter et d'émettre un devis chiffré (avec génération PDF), et à
un **administrateur** de superviser l'activité. Le tout est complété par une
messagerie temps réel et l'automatisation d'e-mails via **n8n**.

Ce dépôt contient le **backend Spring Boot** du projet, livré ici dans une version
**exécutable en local sans aucune installation** : base de données **H2 en mémoire**,
jeu de **données de démonstration** créé au démarrage, et **Swagger** pour tester
toute l'API sans interface graphique.

---

## 1. Prérequis

- **Java 17 ou plus** (`java -version`)
- **Maven** (`mvn -version`)

Rien d'autre : pas de MySQL, pas de configuration manuelle.

## 2. Lancer le projet

```bash
mvn spring-boot:run
```

Au démarrage, la console affiche les liens utiles. Ouvre ensuite dans le navigateur :

- **Swagger (documentation + tests de l'API)** : http://localhost:8080/swagger-ui.html
- **Console H2 (voir la base)** : http://localhost:8080/h2-console
  (JDBC : `jdbc:h2:mem:quoteflow`, utilisateur `sa`, sans mot de passe)

> La base est recréée à zéro à chaque redémarrage (H2 en mémoire).
> Le projet démarre **même si n8n n'est pas lancé** : les appels aux webhooks
> échouent silencieusement sans bloquer le reste. Pour voir les e-mails partir
> réellement, lancer n8n (port 5678) — voir la branche `feature/n8n-workflows`.

## 3. Tester l'API (via Swagger)

Les endpoints sont protégés par **JWT** : il faut donc s'authentifier d'abord.

1. **Créer un compte** : `POST /api/auth/register` (renseigner nom, prénom, email,
   mot de passe).
2. **Se connecter** : `POST /api/auth/login` → la réponse contient un **token**.
3. Dans Swagger, cliquer sur **Authorize** (cadenas en haut à droite) et coller le
   token.
4. Les endpoints protégés (demandes, devis, pièces jointes, messagerie) sont alors
   testables directement.

Parcours fonctionnel type : créer une demande → y joindre un fichier → l'attribuer à
un employé → générer le devis (PDF) → télécharger le PDF. Les PDF générés sont aussi
sauvegardés dans le dossier `./generated/`.

## 4. Sécurité et configuration

- Authentification **stateless par JWT** ; mots de passe hachés en **BCrypt**.
- Accès par rôle (`CLIENT`, `EMPLOYE`, `ADMIN`) via `@PreAuthorize`.
- Endpoints publics : `/api/auth/**`, Swagger, console H2, handshake WebSocket.
- Le **secret JWT** se configure via la variable d'environnement `JWT_SECRET`
  (une valeur de repli est prévue pour le développement local — à remplacer en
  production). Voir `src/main/resources/application.properties`.

## 5. Architecture du dépôt (par module)

Le travail est organisé en branches, une par module :

| Branche | Contenu |
| --- | --- |
| `feature/auth` | **(branche livrée)** Backend complet exécutable : auth/JWT, demandes, devis, PDF, n8n, messagerie temps réel |
| `feature/backend` | Variante backend avec espace admin, tableau de bord et notifications |
| `feature/frontend` | Interface React (espaces client, employé, admin, tableau de bord) |
| `feature/frontend-ouissal` | Interface React (parcours d'authentification et espaces) |
| `feature/n8n-workflows` | Documentation des workflows n8n (confirmation, changement de statut, envoi du devis) |
| `main` | Squelette initial du module d'authentification |

## 6. Équipe et répartition

> ⚠️ **À vérifier / corriger avant remise** — rôles reconstitués d'après l'historique
> du dépôt.

- **Ibtihal Lakhsim** — Authentification & Sécurité (Spring Security + JWT)
- **Meryem Ammari** — Backend QuoteFlow : demandes, devis, génération PDF,
  intégration n8n, messagerie temps réel
- **Douae Derouich** — Authentification & Sécurité (Spring Security + JWT)+Génération et mise en forme des PDF (devis)
- **Ouissal Yahyaoui** — Frontend React (espaces client / employé / admin)

## 7. Stack technique

Java 17 · Spring Boot 3 · Spring Security (JWT) · Spring Data JPA · H2 (dev) ·
iText 7 (PDF) · WebSocket/STOMP · springdoc-openapi (Swagger) · n8n (automatisation) ·
React (frontend, sur branches dédiées).

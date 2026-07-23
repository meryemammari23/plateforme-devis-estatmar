# QuoteFlow — Frontend (React + Vite)

Frontend de la plateforme QuoteFlow, connecté à l'API Spring Boot du dossier `backend`.
Charte graphique inspirée du logo **Statmar** (hexagone teal sur fond bleu nuit, "start smart").

## Installation

```bash
cd quoteflow-frontend
npm install
npm run dev
```

L'application démarre sur **http://localhost:5173**.

## Configuration

Le fichier `.env` définit l'URL de l'API :

```
VITE_API_URL=http://localhost:8080/api
```

Adaptez-le si votre backend Spring Boot tourne sur un autre port. Le `CorsConfig.java`
du backend autorise déjà `http://localhost:5173`.

## Comptes de test

- **Admin** : utilisez l'email/mot de passe définis dans `app.admin.email` /
  `app.admin.password` (application.properties du backend). Un compte admin est
  recréé automatiquement à chaque démarrage du backend.
- **Client** : inscrivez-vous via la page `/register`.
- **Employé** : uniquement créé par un admin depuis l'espace *Employés*.

## Structure

```
src/
├── api/           → axios (intercepteur JWT) + fonctions d'appel par domaine
├── context/        → AuthContext (login/register/logout, session localStorage)
├── components/    → Navbar, StatutBadge, DemandeDetailModal, ProtectedRoute…
├── pages/
│   ├── LoginPage.jsx / RegisterPage.jsx
│   ├── client/     → Nouvelle demande, Mes demandes
│   ├── employe/    → Dossiers attribués, Créer un devis
│   └── admin/      → Dashboard stats, Employés, Attribution des demandes
└── index.css       → design tokens (couleurs, composants UI)
```

## Fonctionnalités par rôle

| Rôle | Fonctionnalités |
|---|---|
| **Client** | S'inscrire/se connecter, soumettre une demande (+ pièces jointes), suivre le statut, télécharger le devis PDF, refuser une demande |
| **Employé** | Voir les dossiers attribués, rédiger un devis (lignes dynamiques, calcul auto du total) |
| **Admin** | Statistiques globales, gestion des comptes employés (créer/activer/désactiver), attribution des dossiers, suivi des demandes en retard |

## Prochaines étapes possibles

- Ajouter la messagerie client ↔ employé (le backend `feature-auth` contient déjà
  un module WebSocket `connect/` — à brancher si besoin).
- Notifications en temps réel (n8n déclenche déjà les emails côté backend).

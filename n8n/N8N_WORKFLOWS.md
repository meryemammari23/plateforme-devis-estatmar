\---



&#x20;2. État d'avancement



&#x20;   | Workflow \& Type de déclencheur 



| 1 | Confirmation de demande | Webhook `POST` 

| 2 | Changement de statut | Webhook `POST` 

| 3 | Envoi du devis PDF | Webhook `POST` 

| 4 | Rappel automatique | Schedule (cron)  ( À développer )



Les trois workflows par webhook ont été validés en environnement de production local

(URLs `/webhook/...`), avec confirmation de réception des emails et historique

d'exécution consultable dans l'onglet \*\*Executions\*\* de n8n.



\---



\## 3. Prérequis \& configuration



\### Environnement



| Composant | Détail 

| n8n | Installé via npm, démarré par `n8n start`, accessible sur `http://localhost:5678` |

| SMTP | Gmail — `smtp.gmail.com`, port `465`, SSL/TLS activé |

| Authentification | Mot de passe d'application Gmail (validation en 2 étapes requise) |



\###  Sécurité des identifiants



Le credential SMTP (mot de passe d'application Gmail) \*\*n'est pas versionné\*\* dans ce

dépôt. Les exports JSON ne contiennent qu'une référence au credential, jamais sa valeur.



\---



\## 4. Référentiel des webhooks



\### 4.1 Confirmation de demande



| Propriété | Valeur 

| \*\*URL (production)\*\* | `http://localhost:5678/webhook/confirmation-demande` |

| \*\*Méthode\*\* | `POST` |

| \*\*Déclenché par\*\* | Soumission d'une nouvelle demande de devis |



\*\*Corps de requête attendu :\*\*



| Champ | Type | Description |

|-------|------|-------------|

| `email` | string | Adresse email du client (destinataire) |

| `nom` | string | Nom du client |

| `reference` | string | Référence de la demande (ex. `QF-2025-0001`) |



\*\*Email généré :\*\* objet « Confirmation de votre demande {reference} ».



\---



\### 4.2 Changement de statut



| Propriété | Valeur |

|-----------|--------|

| \*\*URL (production)\*\* | `http://localhost:5678/webhook/changement-statut` |

| \*\*Méthode\*\* | `POST` |

| \*\*Déclenché par\*\* | Transition de statut d'un dossier |



\*\*Corps de requête attendu :\*\*



| Champ | Type | Description |

|-------|------|-------------|

| `email` | string | Adresse email du client |

| `nom` | string | Nom du client |

| `reference` | string | Référence de la demande |

| `statut` | string | Nouveau statut : `EN\_COURS`, `VALIDE`, `REFUSE` |



\*\*Email généré :\*\* objet « Mise à jour de votre demande {reference} ».



\---



\### 4.3 Envoi du devis PDF



| Propriété | Valeur |

|-----------|--------|

| \*\*URL (production)\*\* | `http://localhost:5678/webhook/envoi-devis` |

| \*\*Méthode\*\* | `POST` |

| \*\*Déclenché par\*\* | Validation et envoi d'un devis par l'employé |



\*\*Corps de requête attendu :\*\*



| Champ | Type | Description |

|-------|------|-------------|

| `email` | string | Adresse email du client |

| `nom` | string | Nom du client |

| `reference` | string | Référence de la demande |

| `pdfUrl` | string | Lien de téléchargement du PDF (ex. `http://localhost:8080/api/devis/1/pdf`) |



\*\*Email généré :\*\* objet « Votre devis {reference} est disponible », incluant le lien

de téléchargement.



\---



\## 5. Procédure de test



Les workflows publiés répondent en permanence sur leurs URLs de production. Exemple de

test du webhook de confirmation depuis PowerShell :



```powershell

Invoke-RestMethod -Uri "http://localhost:5678/webhook/confirmation-demande" `

&#x20; -Method Post -ContentType "application/json" `

&#x20; -Body '{"email":"client@example.com","nom":"Meryem","reference":"QF-2025-0001"}'

```



\*\*Réponse attendue :\*\* `Workflow was started`, suivie de la réception de l'email.

La trace d'exécution est disponible dans l'onglet \*\*Executions\*\* de l'interface n8n.



\---



\## 6. Réimport des workflows



Pour déployer ces workflows sur une autre instance n8n :



1\. Interface n8n → \*\*Overview → Create workflow → menu ⋯ → Import from File\*\*

2\. Sélectionner le fichier `.json` à importer

3\. Recréer le credential SMTP et le lier au nœud \*\*Send an Email\*\*

4\. Publier le workflow pour activer son URL de production



\---



\## 7. Travaux restants



| Workflow 4 — Rappel automatique | Déclencheur cron quotidien interrogeant les dossiers sans réponse depuis X jours, puis envoi d'un email de relance | Endpoint backend listant les dossiers en retard |



| Intégration backend | `N8nWebhookService` (Spring Boot) appelant les 3 webhooks aux événements métier | Module backend Demandes / Devis |




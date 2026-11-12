# dl-ms-enrolment — Backend

Microservice Spring Boot pour la plateforme d'enrôlement BCP. Il gère l'enrôlement des partenaires et clients, le workflow de validation, la gestion des utilisateurs/rôles/permissions, l'audit complet et l'export CSV.

---

## Stack technique

| Composant | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.9 |
| Spring Security + OAuth2 Resource Server | inclus |
| PostgreSQL | 17 |
| Hibernate / JPA | ddl-auto=update (dev) |
| MapStruct | 1.5.5 |
| Lombok | 1.18.30 |
| Springdoc OpenAPI (Swagger) | 2.5.0 |
| Keycloak | 23.0.1 |
| Gradle | Wrapper inclus |

---

## Prérequis

- Java 17+
- PostgreSQL 17 (base `enrollmentdb`)
- Keycloak 23 démarré sur le port `8081`, realm `master`
- Gradle (ou utiliser `./gradlew`)

---

## Démarrage rapide

```bash
# Cloner et compiler
./gradlew clean build

# Démarrer
./gradlew bootRun

# Ou via le jar
java -jar build/libs/dl-ms-enrolment-1.0.0-27032026-dev-0.jar
```

L'API est disponible sur : `http://localhost:8080`  
Swagger UI : `http://localhost:8080/swagger-ui.html`

---

## Variables d'environnement

Toutes les valeurs ont des défauts pour le développement local.

| Variable | Défaut | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | Port HTTP du serveur |
| `DB_HOST` | `localhost` | Hôte PostgreSQL |
| `DB_PORT` | `5432` | Port PostgreSQL |
| `DB_NAME` | `enrollmentdb` | Nom de la base |
| `DB_USER` | `postgres` | Utilisateur DB |
| `DB_PASS` | `madride123` | Mot de passe DB |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8081/realms/master` | Issuer JWT Keycloak |
| `KEYCLOAK_JWK_SET_URI` | `http://localhost:8081/realms/master/protocol/openid-connect/certs` | JWKS Keycloak |
| `KEYCLOAK_TOKEN_URL` | `http://localhost:8081/realms/master/protocol/openid-connect/token` | Endpoint token |
| `KEYCLOAK_CLIENT_ID` | `Enrollement-Frontend` | Client RH SSO |
| `KEYCLOAK_CLIENT_SECRET` | *(variable d'environnement obligatoire si client confidentiel)* | Secret client RH SSO |
| `APP_LOG_LEVEL` | `INFO` | Niveau de log applicatif |

---

## Architecture du projet

```
src/main/java/org/bcp/dlmsenrolment/
├── controller/          # Couche REST — endpoints HTTP
├── services/            # Logique métier (interfaces + impl/)
├── entities/            # Entités JPA (tables DB)
├── dtos/                # Objets de transfert (request/ + respons/)
├── mappers/             # MapStruct : Entity ↔ DTO
├── repositories/        # Spring Data JPA
├── security/            # JWT filter, RBAC filter, SecurityConfig
├── config/              # Beans de configuration Spring
└── excptions/           # Exceptions métier et ApiResponse
```

---

## Endpoints API

### Authentification — `/auth`

| Méthode | URL | Description | Rôle requis |
|---|---|---|---|
| POST | `/auth/login` | Connexion (exchange Keycloak token) | Public |
| POST | `/auth/logout` | Déconnexion | Authentifié |
| POST | `/auth/refresh` | Rafraîchir le token JWT | Authentifié |
| POST | `/auth/change-password` | Changer son mot de passe | Authentifié |

### Partenaires — `/v1/partners`

| Méthode | URL | Description |
|---|---|---|
| GET | `/v1/partners` | Lister tous les partenaires |
| GET | `/v1/partners/{id}` | Détail d'un partenaire |
| POST | `/v1/partners` | Créer un partenaire |
| PUT | `/v1/partners/{id}` | Modifier un partenaire |
| DELETE | `/v1/partners/{id}` | Supprimer un partenaire |

### Workflow de validation — `/v1/validation`

| Méthode | URL | Description |
|---|---|---|
| POST | `/v1/validation/submit/{partnerId}` | Soumettre pour validation |
| POST | `/v1/validation/draft` | Sauvegarder en brouillon |
| POST | `/v1/validation/validate/{id}` | Valider un dossier |
| POST | `/v1/validation/reject/{id}` | Rejeter un dossier |
| GET | `/v1/validation/pending` | Dossiers en attente |
| GET | `/v1/validation/drafts` | Brouillons |
| GET | `/v1/validation/history` | Historique de validation |

### Export — `/v1/export`

| Méthode | URL | Description |
|---|---|---|
| GET | `/v1/export/partners` | Export CSV de tous les partenaires validés (~90 colonnes) |

### Administration Utilisateurs — `/admin/users`

| Méthode | URL | Description |
|---|---|---|
| GET | `/admin/users` | Lister les utilisateurs |
| POST | `/admin/users` | Créer un utilisateur |
| PUT | `/admin/users/{id}` | Modifier un utilisateur |
| DELETE | `/admin/users/{id}` | Supprimer un utilisateur |
| POST | `/admin/users/{id}/roles/{roleId}` | Assigner un rôle |
| DELETE | `/admin/users/{id}/roles/{roleId}` | Révoquer un rôle |

### Administration Rôles — `/admin/roles`

| Méthode | URL | Description |
|---|---|---|
| GET | `/admin/roles` | Lister les rôles |
| POST | `/admin/roles` | Créer un rôle |
| PUT | `/admin/roles/{id}` | Modifier un rôle |
| DELETE | `/admin/roles/{id}` | Supprimer un rôle |
| POST | `/admin/roles/{id}/permissions/{permId}` | Assigner une permission |
| DELETE | `/admin/roles/{id}/permissions/{permId}` | Retirer une permission |

### Administration Permissions — `/admin/permissions`

| Méthode | URL | Description |
|---|---|---|
| GET | `/admin/permissions` | Lister les permissions |
| POST | `/admin/permissions` | Créer une permission |
| PUT | `/admin/permissions/{id}` | Modifier une permission |
| DELETE | `/admin/permissions/{id}` | Supprimer une permission |

### Audit — `/v1/audit`

| Méthode | URL | Description |
|---|---|---|
| GET | `/v1/audit` | Journal complet de toutes les actions |

---

## Sécurité & RBAC

### Flux d'authentification

```
Frontend → POST /auth/login
  → Backend → Keycloak (password grant)
  → Keycloak retourne access_token + refresh_token
  → Backend retourne les tokens au frontend

Frontend → Toutes les requêtes suivantes portent : Authorization: Bearer <access_token>
  → JwtAuthenticationFilter valide le token via JWKS Keycloak
  → PermissionAuthorizationFilter vérifie les permissions RBAC en DB
```

### Profils (rôles)

| Profil | Description |
|---|---|
| `PROFIL_ADMIN_CENTRAL` | Accès complet : gestion users, rôles, permissions, export |
| `PROFIL_SAISIE` | Créer et modifier des partenaires, sauvegarder des brouillons |
| `PROFIL_VALIDATEUR` | Valider ou rejeter les dossiers soumis |
| `PROFIL_CONSULTATION` | Lecture seule |

### Système d'audit

Chaque action sensible est tracée dans la table `audit_logs` avec :
- `username` — acteur de l'action
- `action` — ex: `LOGIN_SUCCESS`, `VALIDATE`, `EXPORT_PARTNERS_CSV`
- `entityType` — ex: `PARTNER`, `USER`, `ROLE`
- `entityId` — ID de l'entité concernée
- `description` — détail en texte libre
- `timestamp` — horodatage

La persistance de l'audit utilise `@Transactional(REQUIRES_NEW)` — l'entrée audit est toujours sauvegardée même si la transaction principale échoue.

**Actions auditées :**
`LOGIN_SUCCESS` · `LOGIN_FAILED` · `LOGOUT` · `PASSWORD_CHANGE` · `TOKEN_REFRESH`  
`CREATE_PARTNER` · `UPDATE_PARTNER` · `DELETE_PARTNER`  
`SUBMIT_VALIDATION` · `SAVE_DRAFT` · `VALIDATE` · `REJECT`  
`CREATE_USER` · `UPDATE_USER` · `DELETE_USER` · `ASSIGN_ROLE` · `REVOKE_ROLE`  
`CREATE_ROLE` · `UPDATE_ROLE` · `DELETE_ROLE` · `ASSIGN_PERMISSION` · `REMOVE_PERMISSION`  
`CREATE_PERMISSION` · `UPDATE_PERMISSION` · `DELETE_PERMISSION`  
`EXPORT_PARTNERS_CSV`

---

## Modèle de données

### Table `partners` — champs principaux

| Groupe | Champs |
|---|---|
| Identité | `name`, `partner_reference_number`, `partner_type`, `partner_status`, `headquarter_country`, `parent_company_country`, `classification`, `account_number`, `canal` |
| Transferts autorisés | `allowed_internal_instant_transfer`, `allowed_external_instant_transfer`, `allowed_standard_transfer`, `allowed_cash_pickup_transfer`, `allowed_mass_cash_pickup_transfer`, `allowed_wallet_transfer` |
| Plafonds | `limit_*_transfer` (6 BigDecimal) + `nature_mass_cash_pickup_transfer`, `valid_days_mass_cash_pickup_transfer` |
| Devise & Change | `main_currency`, `exchange_rate_nature`, `exchange_gain_percentage` |
| Abonnement | `subscription_fee`, `subscription_periodicity`, `subscription_billing_account` |
| Facturation | `payment_nature`, `fee_mode`, `monthly_billing`, `immediate_debit`, `reimbursement` |
| Commissions (×6 types) | `commission_currency` + `[type, amount, rate, min, max]` × `[instant_internal, instant_external, standard_external, mass_cash_pickup, cash_pickup, wallet]` |
| Forex réception | `autoriser_reception`, `devises_reception`, `plafond_reception` |
| Forex émission | `autoriser_emission`, `type_transaction_emission`, `autorisation_oc`, `ref_autorisation_oc`, `devises_emission`, `nature_cours_change_emission`, `plafond_montant_devise` |
| Conformité DO | `conformite_do_type_piece`, `conformite_do_num_piece`, `conformite_do_date_naissance`, `conformite_do_pays_naissance`, `conformite_do_adresse`, `conformite_do_ville_residence` |
| Conformité Bénéf | `conformite_benef_type_piece`, `conformite_benef_num_piece`, `conformite_benef_adresse`, `conformite_benef_ville_residence`, `conformite_benef_pays_residence` |
| Facturation avancée | `fichier_synthese`, `setup_fee`, `setup_fee_billed`, `billing_mode`, `derogation`, `derogation_details` |
| Workflow (enrôlement) | `enrollment_type`, `workflow_status`, `created_by`, `created_at`, `updated_at` |

---

## Migration production

Le fichier `V1__add_enrollment_columns_to_partners.sql` à la racine du projet contient le script SQL à exécuter sur la base de production (qui possède déjà la table `partners` de l'API transfer). Il ajoute uniquement les nouvelles colonnes sans toucher aux colonnes existantes.

```bash
mysql -u root -p enrollmentdb < V1__add_enrollment_columns_to_partners.sql
```

En production, changer `ddl-auto` en `validate` :
```properties
spring.jpa.hibernate.ddl-auto=validate
```

---

## Build & Docker

```bash
# Build JAR
./gradlew clean bootJar

# Docker
docker build -t dl-ms-enrolment:1.0.0 .
docker run -p 8080:8080 \
  -e DB_HOST=db \
  -e KEYCLOAK_ISSUER_URI=http://keycloak:8081/realms/master \
  dl-ms-enrolment:1.0.0
```

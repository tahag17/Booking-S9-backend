# Cas de tests — Authentification & Autorisation
**OAuth2 (Auth0 / Google) & Gestion de session**

---

## Périmètre de test

Les tests couvrent :

- Authentification via OAuth2 (Auth0 + Google)
- Création et persistance de la session côté backend
- Synchronisation des données utilisateur
- Gestion des rôles et des autorisations
- Déconnexion (logout)
- Sécurité des endpoints protégés

---

## Environnement de test

### Pré-requis

- Frontend Angular disponible sur :
  http://localhost:4200


- Backend Spring Boot disponible sur :
  http://localhost:8080


- Application Auth0 configurée avec :
- Connexion Google active
- Redirect URI valide
- Claims personnalisés pour les rôles

- Un compte Google valide pour les tests

---

## Cas de tests — Authentification OAuth2

---

### TC-OAUTH-001 — Connexion OAuth2 réussie avec Google

**Description**  
Vérifier qu’un utilisateur peut se connecter avec succès via Google (Auth0).

**Préconditions**
- L’utilisateur n’est pas authentifié
- Aucune session active dans le navigateur

**Étapes**
1. Ouvrir l’application Angular
2. Cliquer sur le bouton **Login**
3. Choisir **Google** comme méthode de connexion
4. S’authentifier avec un compte Google valide

**Résultat attendu**
- Redirection vers Auth0
- Authentification réussie
- Redirection automatique vers `http://localhost:4200`
- Aucun écran d’erreur backend
- L’utilisateur est connecté dans l’application

**Statut**  
 Non exécuté

---

### TC-OAUTH-002 — Validation du JWT et des claims côté backend

**Description**  
Vérifier que le backend valide correctement le JWT envoyé par le frontend
et extrait les informations d’authentification (utilisateur + rôles) depuis les claims.

**Préconditions**
- Connexion OAuth2 réussie
- Le frontend possède un `access_token` JWT valide

**Étapes**
1. Le frontend envoie une requête HTTP avec l’en-tête :
   Authorization: Bearer <access_token>

2. Appeler l’endpoint backend protégé :
   GET /api/auth/get-authenticated-user

**Résultat attendu**
- Code HTTP `200 OK`
- Le token JWT est validé par Spring Security
- Les claims OAuth2 sont accessibles côté backend
- Le claim personnalisé `https://www.ensas9.fr/roles` est présent
- Les rôles sont correctement mappés en autorités Spring (`ROLE_*`)
- Les informations utilisateur sont correctement retournées

**Statut**  
 Non exécuté

---

### TC-OAUTH-003 — Redirection automatique vers le frontend

**Description**  
Vérifier que l’utilisateur est automatiquement redirigé vers Angular après le login.

**Étapes**
1. Effectuer une connexion OAuth2 complète
2. Observer l’URL finale dans le navigateur

**Résultat attendu**
- URL finale : http://localhost:4200

- Aucun rafraîchissement manuel requis

**Statut**  
 Non exécuté

---

### TC-OAUTH-004 — Annulation de l’authentification OAuth2

**Description**  
Vérifier le comportement lorsque l’utilisateur annule la connexion OAuth2.

**Étapes**
1. Ouvrir l’application Angular
2. Cliquer sur le bouton **Login**
3. Être redirigé vers la page Auth0 / Google
4. Annuler la connexion (fermeture de la fenêtre ou bouton « Annuler »)

**Résultat attendu**
- Redirection vers l’application frontend (Angular)
- Aucun token OAuth2 n’est émis
- Aucune authentification n’est créée côté backend
- L’utilisateur reste dans un état non authentifié
- Aucun message d’erreur technique (stacktrace, exception serveur) n’est affiché à l’utilisateur

**Statut**  
 Non exécuté

---

## 5. Cas de tests — Synchronisation utilisateur

---

### TC-USER-001 — Création d’un nouvel utilisateur lors de la première connexion

**Description**  
Vérifier qu’un nouvel utilisateur est automatiquement créé en base de données
lors de la première authentification OAuth2, à partir des claims fournis par Auth0.

**Préconditions**
- L’utilisateur n’existe pas en base de données
- L’utilisateur possède un compte valide chez Google

**Étapes**
1. Ouvrir l’application Angular
2. Se connecter via OAuth2 (Google / Auth0)
3. Appeler l’endpoint backend :
   GET /api/auth/get-authenticated-user
4. Vérifier la base de données utilisateur

**Résultat attendu**
- Un nouvel utilisateur est créé en base de données
- L’email correspond au claim `email`
- Le prénom et le nom correspondent aux claims (`given_name`, `family_name`)
- Les rôles sont initialisés à partir du claim personnalisé  
  `https://www.ensas9.fr/roles`
- Aucun doublon utilisateur n’est créé

**Statut**  
 Non exécuté

---

### TC-USER-002 — Mise à jour des données utilisateur depuis Auth0

**Description**  
Vérifier que les informations d’un utilisateur existant sont mises à jour
en base de données lorsque les données changent côté Auth0 et qu’une
synchronisation est déclenchée.

**Préconditions**
- L’utilisateur existe déjà en base de données
- L’utilisateur est authentifié via OAuth2
- Les données utilisateur sont modifiées côté Auth0 ou dans le compte Google
  (ex. prénom, nom, image de profil)


**Étapes**
1. Modifier les informations de l’utilisateur dans Auth0
2. Se connecter via OAuth2
3. Appeler l’endpoint backend :
   GET /api/auth/get-authenticated-user?forceResync=true
4. Vérifier les données utilisateur en base de données

**Résultat attendu**
- Les données locales sont mises à jour
- Aucun doublon utilisateur

**Statut**  
 Non exécuté

---

## Cas de tests — Rôles et autorisations

---

### TC-ROLE-001 — Extraction des rôles depuis les claims OAuth2

**Description**  
Vérifier que les rôles sont correctement extraits depuis le token.

**Préconditions**
- L’utilisateur possède des rôles dans Auth0

**Étapes**
1. Se connecter via OAuth2
2. Appeler un endpoint protégé

**Résultat attendu**
- Les rôles sont présents
- Les rôles respectent le format `ROLE_*`

**Statut**  
 Non exécuté

[//]: # (---)

[//]: # ()
[//]: # (### TC-ROLE-002 — Affichage conditionnel côté frontend)

[//]: # ()
[//]: # (**Description**  )

[//]: # (Vérifier que l’interface s’adapte selon les rôles utilisateur.)

[//]: # ()
[//]: # (**Étapes**)

[//]: # (1. Se connecter avec différents rôles)

[//]: # (2. Observer l’interface)

[//]: # ()
[//]: # (**Résultat attendu**)

[//]: # (- Les fonctionnalités s’affichent ou non selon les rôles)

[//]: # ()
[//]: # (**Statut**  )

[//]: # ( Non exécuté)

---

## Cas de tests — Déconnexion

---

### TC-LOGOUT-001 — Déconnexion réussie

**Description**  
Vérifier que l’utilisateur peut se déconnecter correctement.

**Étapes**
1. Se connecter
2. Cliquer sur **Logout**

**Résultat attendu**

- Retour vers le frontend
- Utilisateur déconnecté

**Statut**  
 Non exécuté

---

### TC-LOGOUT-002 — Accès interdit après déconnexion

**Description**  
Vérifier qu’un utilisateur déconnecté ne peut plus accéder aux endpoints protégés.

**Étapes**
1. Se déconnecter
2. Appeler :
   GET /api/auth/get-authenticated-user

**Résultat attendu**
- Code HTTP 401 Unauthorized

**Statut**  
 Non exécuté

---


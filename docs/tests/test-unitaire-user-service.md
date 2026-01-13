# UserServiceTest – Documentation des tests unitaires

## Objectif

Ce document décrit les tests unitaires du service UserService.
Ces tests valident la logique métier liée à la gestion des utilisateurs, à l’authentification OAuth2 et à la synchronisation avec un Identity Provider (IdP), sans dépendre d’une base de données ni du contexte Spring.

## Type de tests

Il s’agit de tests unitaires purs.

## Dépendances simulées (Mocks)

Les composants suivants sont mockés afin d’isoler complètement le service testé :
- UserRepository : accès aux données utilisateur
- UserMapper : transformation entre User et ReadUserDTO
- SecurityContext : contexte de sécurité Spring
- Authentication : informations d’authentification de l’utilisateur

Le service UserService est injecté avec ces mocks afin de tester uniquement sa logique métier.

## Données de test

Avant chaque test, des objets de test sont initialisés :
- Un utilisateur User avec email, nom, prénom, image, rôles et date de modification
- Un ReadUserDTO représentant le résultat attendu
- Un utilisateur OAuth2 simulant un utilisateur authentifié via un fournisseur OAuth2

Ces données permettent de tester différents scénarios de manière contrôlée et reproductible.

## Tests liés à l’utilisateur authentifié

### Récupération de l’utilisateur authentifié – cas nominal

Ce test vérifie que le service :
- récupère correctement l’utilisateur depuis le SecurityContext
- extrait l’email depuis l’utilisateur OAuth2
- retrouve l’utilisateur correspondant en base
- retourne un ReadUserDTO correctement rempli

Le test valide également que le repository et le mapper sont bien appelés.

### Récupération de l’utilisateur authentifié – utilisateur absent

Ce test couvre le cas où l’utilisateur authentifié n’existe pas en base.

Le comportement attendu est le lancement d’une exception indiquant que l’utilisateur est introuvable.
Le mapper ne doit jamais être appelé dans ce cas.

## Tests de récupération par email

### Utilisateur existant

Ce test vérifie que la récupération par email retourne un utilisateur lorsque celui-ci existe.

Le résultat attendu est un Optional contenant un ReadUserDTO valide, avec des informations cohérentes.

### Utilisateur inexistant

Ce test vérifie que le service retourne un Optional vide lorsque l’email ne correspond à aucun utilisateur.

Aucune transformation via le mapper ne doit être effectuée.

## Tests de récupération par identifiant public

### Utilisateur existant

Ce test valide la récupération d’un utilisateur à partir de son identifiant public.

Un Optional contenant le ReadUserDTO attendu doit être retourné.

### Utilisateur inexistant

Ce test vérifie que le service retourne un Optional vide lorsque l’identifiant public n’existe pas.

Le mapper ne doit pas être appelé.

## Tests de synchronisation avec l’Identity Provider (IdP)

Ces tests concernent la synchronisation des données utilisateur avec un fournisseur d’identité externe.

### Création d’un nouvel utilisateur

Lorsque l’utilisateur n’existe pas encore en base, le service doit créer un nouvel utilisateur à partir des informations OAuth2.

Une sauvegarde en base est attendue.

### Mise à jour lorsque la date IdP est plus récente

Si l’utilisateur existe et que la date de mise à jour fournie par l’IdP est plus récente que celle stockée localement, le service doit mettre à jour l’utilisateur.

### Absence de mise à jour lorsque la date IdP est plus ancienne

Si la date fournie par l’IdP est plus ancienne et que la synchronisation n’est pas forcée, aucune mise à jour ne doit être effectuée.

### Mise à jour forcée

Lorsque l’option de synchronisation forcée est activée, l’utilisateur est mis à jour même si la date IdP est plus ancienne.

### Gestion du type Instant pour la date de mise à jour

Ce test vérifie que le service gère correctement une date de mise à jour fournie sous forme d’Instant et pas uniquement sous forme numérique.

### Absence de date de mise à jour

Si l’attribut de date de mise à jour est absent, aucune mise à jour de l’utilisateur ne doit être effectuée.

## Conclusion

Ces tests unitaires garantissent que le service UserService :
- gère correctement l’utilisateur authentifié
- applique correctement les règles métier de synchronisation avec l’IdP
- reste totalement indépendant de la base de données
- respecte les bonnes pratiques des tests unitaires avec mocks

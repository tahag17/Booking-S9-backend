
# Documentation des tests – LandlordServiceTest

## Objectif du fichier

Ce document décrit les tests unitaires du service **LandlordService**.  
Ces tests valident le comportement métier côté propriétaire (landlord) : création d’annonces, récupération, suppression et gestion des droits.

Les tests sont **unitaires**, basés sur **Mockito**, et n’utilisent **aucune base de données réelle**.

---

## Type de tests

- Tests unitaires (JUnit 5)
- Dépendances simulées avec Mockito
- Aucun chargement du contexte Spring
- Pas de base de données, pas de H2

---

## Dépendances mockées

Les composants suivants sont simulés :

- ListingRepository : accès aux annonces
- ListingMapper : conversion entités ↔ DTO
- UserService : récupération de l’utilisateur authentifié
- Auth0Service : gestion des rôles Auth0
- PictureService : gestion des images

Le service testé est **LandlordService**, injecté avec `@InjectMocks`.

---

## Initialisation des données

Avant chaque test :

- Création d’un utilisateur landlord fictif
- Création d’une annonce (Listing)
- Création des DTO nécessaires :
    - SaveListingDTO
    - CreatedListingDTO
    - DisplayCardListingDTO
- Génération d’identifiants UUID

Cela garantit des tests indépendants et reproductibles.

---

## Tests de création d’annonce

### Création réussie

Vérifie que :
- l’annonce est créée correctement
- le landlord est associé à l’annonce
- les images sont sauvegardées
- le rôle LANDLORD est ajouté via Auth0
- un CreatedListingDTO est retourné

### Affectation du landlord

Vérifie que le `landlordPublicId` est bien injecté dans l’annonce lors de la création.

### Gestion des erreurs Auth0

Même si l’ajout du rôle LANDLORD échoue :
- l’annonce est quand même créée
- le service reste fonctionnel

---

## Tests de récupération des annonces

### Récupération des propriétés du landlord

- Retourne la liste des annonces du propriétaire
- Utilise la picture de couverture
- Mappe correctement les résultats en DTO

### Aucun bien existant

- Retourne une liste vide
- Aucun traitement supplémentaire

---

## Tests de suppression

### Suppression autorisée

- L’annonce appartient bien au landlord
- Le statut retourné est OK
- L’identifiant supprimé est renvoyé

### Suppression non autorisée

Cas couverts :
- annonce inexistante
- annonce appartenant à un autre landlord

Résultat :
- statut UNAUTHORIZED
- message d’erreur explicite

---

## Tests de récupération ciblée

### Par identifiant d’annonce

- Retourne les informations nécessaires à une réservation
- Utilise un DTO spécifique (ListingCreateBookingDTO)

### Annonce inexistante

- Retourne Optional.empty
- Aucun mapping effectué

---

## Tests d’affichage carte (cards)

### Récupération par liste d’IDs

- Retourne les cartes correspondantes
- Chaque annonce est mappée individuellement

### Aucun résultat

- Retourne une liste vide
- Aucun mapping inutile

---

## Tests de sécurité propriétaire

### Accès autorisé

- L’annonce appartient bien au landlord
- Retourne un DisplayCardListingDTO

### Accès refusé

- Mauvais landlord ou annonce inexistante
- Retourne Optional.empty

---

## Conclusion

Ces tests garantissent que :

- la logique métier du landlord est correcte
- les droits d’accès sont respectés
- les erreurs externes (Auth0) sont tolérées
- le service est robuste et testable sans infrastructure

Ils constituent une base solide pour la maintenance et l’évolution du service.

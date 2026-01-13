# TenantServiceTest – Documentation des tests unitaires

## Objectif

Ce document décrit les tests unitaires du service TenantService.
Ces tests valident le comportement du service côté locataire (tenant), notamment la consultation des annonces, la récupération du détail d’une annonce et la recherche de logements disponibles selon différents critères.

Les tests vérifient uniquement la logique métier, sans dépendre d’une base de données ni du contexte Spring.

## Type de tests

Il s’agit de tests unitaires.


## Données de test

Avant chaque test, des données communes sont initialisées :
- Une annonce Listing avec ses caractéristiques principales
- Des DTO d’affichage (DisplayCardListingDTO et DisplayListingDTO)
- Un utilisateur propriétaire (ReadUserDTO)
- Un objet Pageable pour tester la pagination
- Une image simulée (PictureDTO)

Ces données permettent de tester différents scénarios de manière cohérente.

## Tests de récupération par catégorie

### Catégorie ALL

Ce test vérifie que lorsque la catégorie demandée est ALL :
- toutes les annonces sont récupérées
- aucune filtration par catégorie n’est appliquée
- les annonces sont correctement transformées en cartes d’affichage

### Catégorie spécifique

Ce test vérifie que lorsqu’une catégorie spécifique est fournie :
- seules les annonces de cette catégorie sont récupérées
- la méthode appropriée du repository est appelée

### Aucune annonce trouvée

Ce test valide le comportement lorsque :
- aucune annonce n’existe pour la catégorie demandée

Le service doit retourner une page vide.

## Tests de récupération d’une annonce par identifiant

### Annonce existante

Ce test vérifie que :
- une annonce existante est correctement récupérée
- les informations du propriétaire sont ajoutées
- le service retourne un état OK avec les données complètes

### Annonce inexistante

Ce test valide que :
- le service retourne un état ERROR
- aucun mapping ni appel au UserService n’est effectué

## Tests de recherche d’annonces disponibles

Ces tests concernent la méthode de recherche avancée.

### Exclusion des annonces déjà réservées

Ce test vérifie que :
- toutes les annonces correspondant aux critères sont récupérées
- les annonces déjà réservées sur la période demandée sont exclues
- seules les annonces disponibles sont retournées

### Aucune annonce réservée

Ce test vérifie que :
- si aucune annonce n’est réservée sur la période
- toutes les annonces correspondantes sont retournées

### Toutes les annonces réservées

Ce test valide que :
- si toutes les annonces sont déjà réservées
- le résultat est une page vide

### Aucun résultat pour les critères

Ce test vérifie que :
- si aucun logement ne correspond aux critères de recherche
- aucune vérification de réservation n’est effectuée
- une page vide est retournée

## Tests complémentaires

### Gestion de la pagination

Ce test vérifie que :
- le service gère correctement le nombre total d’éléments
- le calcul du nombre de pages est cohérent avec la taille de page

### Critères de recherche variés

Ce test valide que :
- le service fonctionne avec différents types de critères
- les recherches restent cohérentes pour des logements de standing différent

## Conclusion

Ces tests unitaires garantissent que TenantService :
- applique correctement les règles de filtrage et de recherche
- exclut correctement les annonces indisponibles
- gère les cas limites (résultats vides, pagination)
- reste indépendant de la base de données et du framework Spring

Ils assurent la fiabilité et la maintenabilité du service côté locataire.

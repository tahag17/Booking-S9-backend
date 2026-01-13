# Documentation — BookingServiceTest

## Objectif du fichier
Ce document explique le **rôle, la structure et le comportement** des tests unitaires du service `BookingService`.
Les tests utilisent **Mockito** et **JUnit 5** et ne nécessitent **aucune base de données**, car toutes les dépendances sont mockées.

---

## Type de test
- **Test unitaire**
- Cible : logique métier du `BookingService`
- Dépendances simulées avec des mocks

---

## Dépendances mockées et rôle

- **BookingRepository**  
  Simule l’accès à la base de données pour les réservations (création, suppression, recherche).

- **BookingMapper**  
  Transforme les DTO en entités métier et inversement.

- **UserService**  
  Fournit l’utilisateur actuellement authentifié (tenant ou landlord).

- **LandlordService**  
  Permet de récupérer les informations des logements (prix, propriété, affichage).

- **SecurityUtils (mock statique)**  
  Simule les rôles de sécurité (ROLE_LANDLORD).

---

## Données de test initialisées

Avant chaque test :
- Identifiants UUID pour tenant, listing et booking
- Dates de réservation (start / end)
- Utilisateur connecté (tenant)
- Objet `Booking`
- DTOs nécessaires à la création et à l’affichage

Cela garantit des tests **indépendants et reproductibles**.

---

## Explication des groupes de tests

### 1. Création de réservation (`create`)

#### Cas valide
- Le logement existe
- Aucune réservation sur la période
- Le prix total est correctement calculé
- La réservation est sauvegardée
- Résultat : **Status OK**

#### Cas d’échec
- Logement introuvable → erreur
- Réservation déjà existante → erreur
- Aucune écriture en base simulée

---

### 2. Vérification de disponibilité (`checkAvailability`)
- Retourne les périodes déjà réservées pour un logement
- Transforme les entités `Booking` en `BookedDateDTO`

---

### 3. Réservations du tenant (`getBookedListing`)
- Récupère les réservations du tenant connecté
- Associe chaque réservation à une carte logement
- Vérifie la cohérence des données affichées

---

### 4. Annulation de réservation (`cancel`)

#### Annulation par le tenant
- Autorisée uniquement sur ses propres réservations
- Succès si la réservation existe

#### Annulation par le landlord
- Vérifie le rôle de sécurité
- Vérifie que le logement appartient au landlord
- Supprime la réservation associée

#### Cas d’échec
- Réservation inexistante
- Logement ne принад pas au landlord

---

### 5. Réservations côté landlord (`getBookedListingForLandlord`)
- Récupère tous les logements du landlord
- Récupère toutes les réservations associées
- Retourne une vue globale des réservations

---

### 6. Recherche par dates (`getBookingMatchByListingIdsAndBookedDate`)
- Filtre les logements ayant une réservation sur une période donnée
- Retourne uniquement les identifiants correspondants

---

### 7. Calcul du prix total
- Vérifie que le prix est correctement calculé :
    - nombre de nuits × prix par nuit
- Cas de réservation longue durée testé



## Conclusion

Ce fichier de tests garantit :
- la fiabilité du processus de réservation
- le respect des règles métier
- la sécurité des actions (tenant / landlord)
- un calcul correct des prix

Il constitue une **base solide de validation métier** du `BookingService`.

# Cahier des charges  
## Projet : AirportManagement – Simulation d’aéroport concurrent

**Auteur :** Nerimen Arfaoui 

**Date :** 05/12/2025

---

## 1. Contexte et objectifs

### 1.1. Contexte

Dans un aéroport réel, plusieurs avions doivent partager un nombre limité de ressources :

- Pistes d’atterrissage/décollage,
- Portes d’embarquement/débarquement.

Ces ressources sont utilisées de manière concurrente et doivent être gérées de façon sûre (pas de collision) et équitable (priorité aux arrivées, éviter l’attente infinie).

### 1.2. Objectifs pédagogiques et techniques

- Mettre en œuvre la **programmation concurrente** en Java.
- Comparer différents mécanismes de synchronisation :
  - Moniteurs (`synchronized`, `wait`, `notifyAll`),
  - Sémaphores (`Semaphore`),
  - Verrous réentrants et conditions (`ReentrantLock`, `Condition`).
- Concevoir et réaliser une **interface graphique** (JavaFX) permettant de visualiser :
  - l’état des pistes et des portes,
  - les files d’attente des avions,
  - les événements de la simulation (logs).
- Produire une **documentation** et un **rapport comparatif**.

---

## 2. Périmètre fonctionnel

### 2.1. Entités principales

- **Avion (Airplane)**
  - Identifiant unique (`A1`, `A2`, …)
  - Type de vol : `ARRIVEE` ou `DEPART`
  - État :
    - EN_VOL, ATTENTE_ATTERRISSAGE, ATTERRISSAGE,
    - ATTENTE_PORTE, A_LA_PORTE,
    - ATTENTE_DECOLLAGE, DECOLLAGE, AU_SOL, TERMINE
  - Comportement simulé par un thread Java.

- **Piste (Runway)**
  - Identifiant (ex : 1, 2, …)
  - Occupée par au plus un avion à la fois.

- **Porte (Gate)**
  - Identifiant (ex : 1, 2, …)
  - Occupée par au plus un avion à la fois.

- **Aéroport (Airport)**
  - Nombre configurable de pistes (par défaut 2).
  - Nombre configurable de portes (par défaut 4).
  - Gestion des files d’attente :
    - file d’attente pour les arrivées (avant atterrissage),
    - file d’attente pour les départs (avant décollage).
  - Délégation de la politique de synchronisation à une interface `AirportSynchronization`.

---

### 2.2. Fonctionnalités utilisateur (GUI)

#### 2.2.1. Visualisation des ressources

- **Zone “Pistes”**
  - Affiche chaque piste comme un rectangle coloré.
  - Couleurs :
    - Vert : piste libre
    - Rouge : piste occupée
  - Texte associé :
    - `Piste 1` (libre)
    - `Piste 1 : A3` (occupée par l’avion A3)

- **Zone “Portes”**
  - Affiche chaque porte comme un rectangle coloré.
  - Couleurs :
    - Bleu : porte libre
    - Orange : porte occupée
  - Texte associé :
    - `Gate 1`
    - `Gate 1 : A5`

#### 2.2.2. Visualisation des files d’attente

- Deux listes (ListView JavaFX) :
  - File d’attente des **arrivées**.
  - File d’attente des **décollages**.
- Chaque ligne contient au minimum :
  - l’ID de l’avion (`A5`)
  - et son état (`ATTENTE_ATTERRISSAGE`, `ATTENTE_DECOLLAGE`, etc.).

#### 2.2.3. Journal des événements (Logs)

- Une zone texte (TextArea) affiche les logs de la forme :
[10:32:01] Création avion A3 (ARRIVEE)
[10:32:02] Avion A3 en vol.
[10:32:10] Avion A3 atterrit sur piste 1
[10:32:20] Avion A3 à la porte 2
- Les logs sont mis à jour de manière thread-safe via `Platform.runLater`.

#### 2.2.4. Contrôle de la simulation

Dans un panneau de contrôle (“Control Tower”) :

- **Sélection du mécanisme de synchronisation**
- 3 RadioButtons :
  - Moniteur
  - Sémaphore
  - ReentrantLock
- Changement de sélection ⇒ relance de l’aéroport avec la nouvelle politique de synchro.

- **Paramétrage**
- Spinner pour le nombre de pistes (1–5).
- Spinner pour le nombre de portes (1–10).
- Slider pour la **vitesse de simulation** (0.5× à 3×).

- **Création d’avions**
- Bouton “Ajouter ARRIVÉE”
  - Crée un avion de type ARRIVEE et démarre son thread.
- Bouton “Ajouter DÉPART”
  - Crée un avion de type DEPART et démarre son thread.

- **Stress test**
- Bouton “Stress test”
  - Crée un lot d’avions (ex : 10 arrivées + 5 départs) pour tester la résistance de la synchronisation.

---

## 3. Exigences techniques

### 3.1. Technologies

- Langage : **Java** (version 11 ou supérieure recommandée).
- Interface graphique : **JavaFX**.
- IDE cible : **Eclipse** (mais le projet reste standard Maven/Gradle ou simple projet Java).
- Gestion de la concurrence :
- `java.lang.Thread`
- `synchronized`, `wait`, `notifyAll`
- `java.util.concurrent.Semaphore`
- `java.util.concurrent.locks.ReentrantLock`, `Condition`

### 3.2. Structure du projet

Packages principaux :

- `airport.core`  
- Classe `Airport`, interface `AirportObserver`, logique centrale.
- `airport.model`  
- `Airplane`, `Runway`, `Gate`, `AirplaneState`, `FlightType`.
- `airport.sync`  
- `AirportSynchronization` (interface),
- `MonitorSynchronization`, `SemaphoreSynchronization`, `LockConditionSynchronization`.
- `airport.gui`  
- `MainApp` (JavaFX), contrôles et visualisation.
- `airport.utils`  
- `SimLogger`, classes utilitaires.

---

## 4. Contraintes et critères d’évaluation

### 4.1. Contraintes de synchronisation

- **Sécurité**
- Aucune piste ne doit accueillir plus d’un avion à la fois.
- Aucune porte ne doit accueillir plus d’un avion à la fois.
- Pas d’accès concurrent non contrôlé aux structures partagées (`runways`, `gates`, files d’attente).

- **Absence de deadlock**
- La simulation ne doit jamais se figer définitivement.
- L’exécution doit continuer même en cas de forte charge (stress test).

- **Priorité aux arrivées**
- Lorsqu’il y a au moins une arrivée en attente de piste, aucun départ ne doit pouvoir obtenir une piste tant que ces arrivées ne sont pas servies.

- **Équité (pas de starvation)**
- Les départs doivent finir par avoir accès à une piste lorsque plus aucune arrivée n’est en attente.

### 4.2. Critères d’évaluation

1. **Conformité fonctionnelle**
 - Toutes les fonctionnalités décrites au §2.2 sont présentes et opérationnelles.

2. **Qualité de la synchronisation**
 - Pas de crash ni de blocage.
 - Priorité aux arrivées respectée dans les trois implémentations.
 - Pas de violations de règles (deux avions sur la même resource, etc.).

3. **Qualité de l’architecture**
 - Interface `AirportSynchronization` bien séparée des implémentations.
 - GUI indépendante du détail de la synchronisation (choix dynamique du mécanisme).
 - Code modulable et réutilisable.

4. **Qualité de l’interface utilisateur**
 - Visualisation claire de l’état des ressources.
 - Affichage correct des files d’attente.
 - Logs lisibles (horodatés).

5. **Documentation**
 - Cahier des charges (ce document).
 - Rapport comparatif des trois versions de synchronisation.
 - Commentaires dans le code pour les parties critiques (synchronisation, création de threads…).

---

## 5. Plan de travail (proposition)

1. **Phase 1 – Modélisation (model + core)**
 - Définition des classes `Airplane`, `Runway`, `Gate`, `Airport`.
 - Définition de l’interface `AirportSynchronization`.

2. **Phase 2 – Implémentation des synchronisations**
 - Version Moniteur (`MonitorSynchronization`).
 - Version Sémaphore (`SemaphoreSynchronization`).
 - Version ReentrantLock (`LockConditionSynchronization`).

3. **Phase 3 – Interface graphique**
 - Mise en place de la fenêtre JavaFX principale.
 - Ajout des panneaux Pistes, Portes, Files d’attente, Logs, Contrôle.

4. **Phase 4 – Intégration et tests**
 - Connexion des threads d’avions à la GUI via `AirportObserver`.
 - Tests unitaires de base sur les ressources.
 - Stress tests et tests de priorité.

5. **Phase 5 – Documentation**
 - Rédaction du présent cahier des charges.
 - Rédaction du rapport comparatif.
 - Préparation des captures d’écran et exemples de scénarios.

---

## 6. Livrables

- **Code source complet** du projet `AirportManagement`.
- **Executable / instructions** pour lancer la simulation.
- **CahierDesCharges.md** (ce document).
- **RapportComparatif.md** (à convertir en PDF).
- Éventuellement :
- Diagrammes de classes (UML).
- Scénarios de tests documentés.

---

# Projet annuaire.mg

**Projet transversal — développement mobile (M1)**  
Dépôt : [github.com/MaheryJeremie/annuaire.mg](https://github.com/MaheryJeremie/annuaire.mg)  
Cadrage produit : [`Annuaire_mg.md`](Annuaire_mg.md) · Guide technique : [`SYNC.md`](../SYNC.md)

| Auteurs | Rôle |
|---------|------|
| MaheryJeremie | Architecture, sync, navigation, documentation |
| Rehareha Ranaivo | Recherche visiteur, espace prestataire, photos CIN, UX fiche |

---

## Sommaire

- **Introduction**
- **Contexte du projet à Madagascar**
- **Problématique et opportunité**
  - La problématique actuelle
  - L’opportunité de transformation
- **Objectifs du projet**
  - Analyse de l’existant et des besoins
  - Étude du domaine concerné : services de proximité
  - Solutions existantes (locales ou internationales)
  - Cible et utilisateurs
  - Cas d’usages / Scénarios
- **Spécifications et conception**
  - Application mobile
    - Accueil
    - Fiche prestataire
    - Aide
    - Compte
    - Connexion et inscription
    - Espace prestataire
    - Ma fiche
    - Vérification CIN
- **Architecture de déploiement**
  - Application Android
  - Firebase Realtime Database
  - Base locale Room
  - Photos
  - Modèle de données
- **Gestion du projet**
  - Outils et méthodes de suivi
  - Tableau des estimations de tâches
- **Annexes**
  - Annexe A — Application web commune
  - Annexe B — Comptes de démonstration
  - Annexe C — Règles Firebase
  - Annexe D — Correspondance avec le cours

---

## Introduction

Trouver un plombier, un coiffeur ou un mécanicien à Madagascar passe encore trop souvent par un **groupe Facebook** : on publie une demande, on attend, on négocie le prix en message privé, et on espère que le numéro soit encore valide.

**annuaire.mg** inverse cette logique. C’est un **annuaire public des prestataires de proximité** : le visiteur parcourt une liste, filtre par métier et quartier, voit les **tarifs**, la **disponibilité** et les **avis**, puis **appelle** ou envoie un **SMS**. Les profils **certifiés** ont été contrôlés par la commune (CIN), via un petit outil web — pas dans le téléphone.

Ce n’est pas une marketplace privée, ni un clone de Facebook, ni un Uber. C’est un **registre à caractère communal**, consultable sans compte, pensé pour un usage réel : connexion irrégulière, téléphone Android, besoin d’appeler tout de suite.

L’application livrée n’a que **deux espaces** :

| Espace | Qui | Où |
|--------|-----|----|
| **Visiteur** | Tout le monde, sans inscription | App Android |
| **Prestataire** | Fiche publique + envoi du CIN | App Android |
| **Commune** | Validation CIN et métiers | Outil web (annexe A) |

---

## Contexte du projet à Madagascar

Le secteur informel et les métiers de proximité occupent une place centrale dans la vie quotidienne : réparation, coiffure, plomberie, électricité, mécanique. La géographie utile n’est pas seulement la ville, c’est le **quartier / fokontany**. On cherche « un électricien à Alarobia », pas « un prestataire à 12 km ».

Le canal dominant pour ces mises en relation reste **Facebook**. Les groupes d’entraide sont actifs, mais l’information y est éphémère : le post disparaît, le prix n’est pas affiché, le même artisan n’est pas retrouvable le mois suivant.

La **connectivité** est inégale. Un service qui n’existe que en ligne, sans copie locale, est peu fiable dès que le Wi‑Fi manque ou que les données mobiles sont rationnées. En revanche, le **smartphone Android** est l’équipement le plus répandu pour appeler et envoyer un SMS.

Les **communes** ont un rôle de recensement et de confiance (identité, CIN). Elles n’ont pas besoin d’une application mobile lourde : un back-office web suffit pour valider un dossier. D’où le découpage du projet : **l’usage quotidien est dans le téléphone** ; **la décision administrative est sur le web**.

---

## Problématique et opportunité

### La problématique actuelle

Aujourd’hui, pour un service de proximité :

- l’information est **dispersée** (posts, commentaires, messages privés) ;
- les **prix** sont opaques jusqu’à l’échange en MP ;
- il n’y a pas de structure **métier + quartier** vraiment cherchable ;
- la **confiance** est faible (numéros morts, identités non vérifiées) ;
- hors ligne, Facebook n’aide plus.

Cette méthode fait perdre du temps au citoyen et n’offre aucun cadre officiel au prestataire sérieux.

### L’opportunité de transformation

annuaire.mg transforme cette contrainte en service public de proximité.

**Pour le visiteur**

- Liste immédiate, sans compte
- Filtres métier, quartier, certifiés, disponible aujourd’hui
- Tarifs et avis visibles **avant** l’appel
- Appel et SMS natifs (Intents Android)
- Catalogue déjà sur le téléphone (Room) si le réseau manque

**Pour le prestataire**

- Une fiche unique, retrouvable
- Plusieurs tarifs (nom + montant en Ariary)
- Quartiers d’intervention en sélection multiple
- Badge **Certifié** après contrôle communal du CIN

**Pour la commune**

- Contrôle d’identité (CIN recto / verso) et des noms de métiers
- Sans imposer un troisième rôle dans l’application mobile

---

## Objectifs du projet

**Objectif général :** offrir un annuaire officiel, cherchable et consultable hors ligne, qui remplace la recherche artisanale via Facebook pour les services de proximité.

**Objectifs spécifiques :**

1. **Rendre l’information structurée** — métier, quartier, tarifs, disponibilité, avis.
2. **Réduire le frottement** — ouvrir l’app, voir la liste, appeler.
3. **Instaurer un minimum de confiance** — badge Certifié après validation du CIN par la commune.
4. **Tenir compte du réseau** — Room offline-first ; synchronisation Wi‑Fi par défaut, réglable.
5. **Couvrir le programme** — Kotlin, Compose, Navigation, MVVM, Room, Coroutines/Flow, Intents.

### Analyse de l’existant et des besoins

Il n’existe pas, à l’échelle d’un quartier, d’annuaire public simple où l’on voit le prix, le métier et un signe de vérification communale.

**Besoins identifiés**

- Consulter sans s’inscrire
- Filtrer par métier et par quartier (pas seulement par commune)
- Voir des tarifs indicatifs avant d’appeler
- Distinguer un profil contrôlé (CIN) d’un profil non vérifié
- Continuer à lire l’annuaire hors ligne
- Permettre au prestataire de publier et de mettre à jour sa fiche
- Laisser à la commune un outil léger pour valider, pas une app mobile supplémentaire

### Étude du domaine concerné : services de proximité

**Particularités locales**

- Géographie utile = **fokontany / quartier**
- Contact réel = **appel et SMS**, pas un chat in-app
- Confiance = **identité (CIN)**, pas un agrément ministériel
- Réseau = Wi‑Fi quand il y en a, données mobiles parfois évitées

**Opportunités**

- Habitude déjà ancrée de chercher « quelqu’un du quartier »
- Android largement disponible
- Firebase permet un backend sans serveur à administrer pour un MVP académique
- La commune peut valider depuis un navigateur

### Solutions existantes (locales ou internationales)

| Solution | Intérêt | Limite pour ce besoin |
|----------|---------|------------------------|
| Groupes Facebook | Déjà utilisés | Info éphémère, prix caché, pas hors ligne |
| Pages jaunes / annuaires papier | Structurés | Peu à jour, pas de dispo du jour |
| Google / Maps | Géolocalisation | Peu de petits prestataires informels, pas de CIN communal |
| Marketplaces (ex. services à domicile) | Mise en relation | Compte obligatoire, logique privée, souvent paiement / chat |
| WhatsApp | Contact direct | Pas de registre public cherchable |

Ces outils montrent qu’il y a un besoin. Aucun ne combine **registre public + tarifs + quartier + badge communal + hors ligne**.

### Cible et utilisateurs

**Visiteur (citoyen)**  
Ouvre l’app, parcourt, filtre, consulte une fiche, appelle ou SMS, peut laisser un avis. Pas de compte.

**Prestataire**  
Crée un compte téléphone + mot de passe, complète sa fiche (photo, métier, quartiers, tarifs, dispo), envoie son CIN. Compte démo : `0341111111` / `demo123`.

**Agent communal**  
N’utilise **pas** l’app Android. Il se connecte à l’outil web (annexe A) pour valider ou refuser les dossiers CIN et les métiers proposés. Compte démo : `0320000000` / `agent123`.

### Cas d’usages / Scénarios

**1. Trouver un prestataire aujourd’hui**

- Un habitant ouvre **Accueil** : la liste est déjà là.
- Il peut cocher **Dispo aujourd’hui** et **Certifiés**, ou taper un métier / un quartier.
- Il ouvre une fiche, lit les tarifs, appuie sur **Appeler**.

**2. Publier sa fiche**

- Un artisan s’inscrit dans **Compte**.
- Il choisit son métier (suggestions) ou en propose un nouveau.
- Il tape ses **quartiers** (suggestions, plusieurs possibles), ajoute **plusieurs tarifs** avec le bouton **+**.
- Il n’a pas à choisir une commune.
- Il enregistre. Sa fiche devient visible dans l’annuaire (localement tout de suite ; en ligne après sync).

**3. Obtenir le badge Certifié**

- Le prestataire envoie numéro CIN + photos recto / verso.
- La **commune correspondante** (d’après ses quartiers) lui est **assignée et indiquée** ; il ne la choisit pas.
- L’agent communal voit le dossier dans l’outil web, photos comprises.
- Il valide. À la prochaine synchronisation du téléphone, le visiteur voit **Certifié**.

**4. Connexion faible**

- Sans réseau autorisé, l’app sert le catalogue déjà en Room (ou le JSON d’assets au premier lancement).
- Dans **Compte**, l’utilisateur choisit Wi‑Fi uniquement, données mobiles, ou n’importe quel réseau.

---

## Spécifications et conception

Le livrable principal est l’**application Android**. L’interface web de la commune est volontairement hors de ce périmètre visuel : elle est décrite en **annexe A**.

Navigation mobile : barre du bas **Accueil / Aide / Compte**. Parcours visiteur : **liste → détail**. Aucun écran « agent » dans l’app.

```
Accueil ──► Fiche (appel / SMS / avis)
Aide
Compte ──► Connexion / Inscription ──► Espace prestataire
                                      ├── Ma fiche
                                      └── Vérification CIN
```

### Application mobile

#### Accueil

Écran de départ. La liste des prestataires s’affiche **immédiatement** (liste-first) : un filtre n’est pas obligatoire.

| Élément | Comportement |
|---------|----------------|
| En-tête | Logo + nom **annuaire.mg** |
| Puces | **Certifiés**, **Dispo aujourd’hui**, **Métier & quartier** |
| Métier | Champ à suggestions |
| Quartier | Champ à suggestions (homonymes précisés par la commune) |
| Carte | Photo, nom, métier, quartier, note, tarif indicatif (« dès … Ar »), badge Certifié s’il y a lieu |
| Compteur | Nombre de prestataires affichés |

Pas de filtre commune sur cet écran : on cherche par **quartier**.

#### Fiche prestataire

Ouverte depuis une carte de la liste.

| Bloc | Contenu |
|------|---------|
| Identité | Photo, nom, métier, note moyenne, badge **Certifié** uniquement (pas d’« en attente » public) |
| Dispo | Disponible / indisponible aujourd’hui |
| Zone | Commune + liste des quartiers |
| Présentation | Texte libre du prestataire |
| Tarifs | Toutes les lignes (libellé + Ariary), mention « indicatifs » |
| Contact | Boutons **Appeler** et **SMS** (Intents `ACTION_DIAL` / `ACTION_SENDTO`) |
| Avis | Liste existante + formulaire (nom, note 1–5, commentaire) |

#### Aide

Texte pédagogique : comment chercher, ce que signifie **Certifié**, le fait qu’un compte n’est utile qu’aux prestataires, le réglage réseau, les questions fréquentes (CIN non public, avis, métier en validation).

#### Compte

Deux états.

**Non connecté**

- Rappel : on peut chercher sans compte
- **Se connecter** / **Créer un compte prestataire**
- Retour à la recherche
- Carte **Synchronisation** : Wi‑Fi uniquement (défaut) / données mobiles / n’importe quel réseau

**Connecté prestataire**

- Accès à **Mon espace prestataire**
- Déconnexion
- Même réglage réseau

Un compte **agent** qui tente de se connecter dans l’app est **refusé** : la commune travaille dans l’outil web.

#### Connexion et inscription

- Téléphone + mot de passe
- Inscription prestataire : nom, téléphone, mot de passe
- Après succès : espace prestataire

#### Espace prestataire

Tableau de bord du compte :

1. **Ma fiche** — photo, métier, quartiers, tarifs, disponibilité
2. **Vérification CIN** — numéro + recto + verso
3. Lien vers l’aide
4. Avis reçus (y compris le statut interne En attente / Refusé / Certifié, visible seulement ici)

#### Ma fiche

Formulaire public.

| Champ | Comportement |
|-------|----------------|
| Photo de profil | Publique, distincte du CIN |
| Métier | Suggestions ; si le nom n’existe pas, proposition à la commune (`PENDING`) |
| Quartiers | Champ libre + autocomplétion, **plusieurs** ; puces des quartiers choisis |
| Commune | Absente de ce formulaire. Assignée à l’envoi du CIN d’après les quartiers |
| Présentation | Texte multiligne |
| Tarifs | Une ou plusieurs lignes **nom + montant (Ar)** ; bouton **Ajouter un tarif** ; retrait d’une ligne s’il en reste plus d’une |
| Dispo | Interrupteur « Disponible aujourd’hui » |

Enregistrement → Room. La fiche est celle que le visiteur verra.

#### Vérification CIN

- Commune de vérification : **affichée seulement**, déduite des quartiers (pas de liste à choisir)
- Numéro CIN
- Photos recto et verso (stockage local + envoi distant pour la commune)
- Statut du dossier
- Bouton **Envoyer le dossier** → Room `PENDING` + nœud Firebase `dossiers/{id}` (avec URLs des photos) ; la commune est écrite à ce moment

Les visiteurs **ne voient jamais** ces photos.

---

## Architecture de déploiement

Le projet n’utilise pas de VM ni de conteneurs. Le MVP s’appuie sur **Firebase Realtime Database (REST)** comme backend distant, et **Room** comme source de vérité sur le téléphone.

```
┌─────────────────────┐
│  UI Jetpack Compose │
│  Accueil / Fiche /  │
│  Compte / Prestataire│
└──────────┬──────────┘
           │ ViewModel
           ▼
┌─────────────────────┐
│     Repository      │
└──────────┬──────────┘
           │
     ┌─────┴──────┐
     ▼            ▼
┌─────────┐  ┌──────────────────────────┐
│  Room   │  │ Firebase RTDB (REST)     │
│  (offline│  │ catalog / dossiers /     │
│   first) │  │ photos / metiers_proposes│
└─────────┘  └────────────┬─────────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │ Outil web commune│
                 │ (annexe A)       │
                 └─────────────────┘
```

### Application Android

| Couche | Choix | Pourquoi |
|--------|--------|----------|
| Langage | Kotlin | Cours, null-safety |
| UI | Jetpack Compose | Écrans déclaratifs |
| Navigation | Navigation Compose | Accueil → détail ; Compte → prestataire |
| Architecture | MVVM | UI / logique / données séparés |
| Asynchrone | Coroutines + Flow | Listes réactives, sync |
| Local | Room (`annuaire_mg.db`) | Source de vérité hors ligne |
| Réseau | Retrofit + Moshi | REST Firebase, sans SDK Firebase Android |
| Système | Intents | Appel, SMS |

L’URL de la base et les identifiants d’upload photo sont dans `local.properties` (**non commité**).

Synchronisation (réglage Compte) :

| Mode | Comportement |
|------|----------------|
| Wi‑Fi uniquement | Défaut : pas de sync sur données mobiles |
| Données mobiles uniquement | Inverse |
| N’importe quel réseau | Sync dès qu’il y a une connexion |

Si le réseau n’est pas autorisé, ou si l’API échoue : fallback `app/src/main/assets/catalog.json`.

L’app lit `{baseUrl}catalog.json`, puis la **racine** `{baseUrl}.json` si le nœud `catalog` est vide. Elle fusionne ensuite `photos`, `dossiers` et `metiers_proposes`.

### Firebase Realtime Database

Service managé Google, région typique `europe-west1`. Pas de `google-services.json` : uniquement REST.

| Nœud | Rôle |
|------|------|
| `catalog` ou racine | Catalogue (communes, quartiers, métiers, prestataires, tarifs) |
| `photos/{id}` | URL de la photo de profil |
| `dossiers/{id}` | Décision CIN + `cinRectoUrl` / `cinVersoUrl` |
| `metiers_proposes/{id}` | Décision sur un nom de métier proposé |
| `prestataires/{index}` | Statut catalogue (si les règles l’autorisent) |

**Raisons du choix**

- Mise en place rapide pour un MVP académique
- Lecture/écriture JSON simple depuis Android et depuis le HTML commune
- Pas d’administration de serveur
- Suffisant pour catalogue + dossiers, sans paiement ni chat

Limite assumée : ce n’est pas une API métier versionnée ; les règles d’écriture doivent rester ouvertes sur les nœuds utiles (voir annexe C).

### Base locale Room

Room est la **source de vérité**. L’UI ne lit pas Firebase directement.

Avantages :

- l’annuaire reste lisible dans un taxi sans réseau ;
- les écritures prestataire (fiche, avis, CIN) sont d’abord locales ;
- la sync distante est un complément, pas un prérequis à l’ouverture de l’app.

### Photos

- **Profil** : visible dans l’annuaire ; URL dans `photos/{id}`.
- **CIN** : uniquement dans `dossiers/{id}` ; l’outil web les affiche ; jamais sur la fiche publique.
- L’interface utilisateur ne parle pas d’hébergeur d’images.

### Modèle de données

```
User 1 ── 0..1 Prestataire
Metier 1 ── * Prestataire
Commune 1 ── * Quartier
Commune 1 ── * Prestataire
Prestataire * ── * Quartier     (prestataire_quartiers)
Prestataire 1 ── * Tarif
Prestataire 1 ── * Avis
```

| Entité | Rôle |
|--------|------|
| `User` | Compte prestataire (téléphone, mot de passe, rôle) |
| `Metier` | Nom validé ou `PENDING` / `REJECTED` |
| `Commune` | Zone administrative |
| `Quartier` | Fokontany, rattaché à une commune |
| `Prestataire` | Fiche, CIN, `certificationStatus` |
| `PrestataireQuartier` | Table de liaison (plusieurs quartiers) |
| `Tarif` | Plusieurs lignes : libellé + `montantAr` |
| `Avis` | Note 1–5 + commentaire |

Statuts de certification : `NONE`, `PENDING`, `CERTIFIED`, `REJECTED`.  
Seul **Certifié** est public.

Chaîne du badge :

1. Prestataire envoie le CIN → Room `PENDING` + `dossiers/{id}`
2. Commune valide dans le web → même nœud `CERTIFIED` (ou `REJECTED`)
3. Prochaine sync Android → le visiteur voit le badge

---

## Gestion du projet

### Outils et méthodes de suivi

| Outil | Usage |
|-------|--------|
| Git / GitHub | Dépôt public, historique, revues |
| Branches | `main`, `dev`, `feat/…` |
| Pull requests | Une fonctionnalité = une branche = une PR |
| Android Studio | Compilation, émulateur |
| Firebase Console | Base RTDB, règles |
| Markdown | Cadrage (`Annuaire_mg.md`) et ce document |

**Règle d’équipe :** partir de `main` → travailler sur `feat/…` → **PR vers `dev`** → si OK, **PR vers `main`**. On ne pousse pas une feature directement sur `main`.

Repo : https://github.com/MaheryJeremie/annuaire.mg  
Période : **3 – 10 septembre 2026** (semaine technique).

L’outil web commune vit **à côté** du dépôt Android (`annuaire-commune-web/`), volontairement hors Git mobile.

### Tableau des estimations de tâches

| Tâche | Estimation | Qui |
|-------|------------|-----|
| Initialisation Android, Room, catalogue local | 1 j | MaheryJeremie |
| Sync catalogue Wi‑Fi + accès Firebase | 1 j | MaheryJeremie |
| Recherche visiteur, fiche, filtres, avis, appel / SMS | 1 j | Rehareha Ranaivo |
| Espace prestataire, inscription, envoi CIN | 1 j | Rehareha Ranaivo |
| Navigation Accueil / Aide / Compte | 1 j | MaheryJeremie |
| Photos CIN en ligne + réglage réseau | 0,5 j | Rehareha Ranaivo |
| UX fiche : quartiers multi-select, tarifs multiples | 0,5 j | Rehareha Ranaivo |
| Cadrage produit + documentation de présentation | 1 j | MaheryJeremie |
| Outil web commune (hors dépôt Android) | 1 j | Équipe |

Suivi Git : commits nominatifs sur les branches `feat/…`, revus par PR (`dev` puis `main`).

---

## Annexes

### Annexe A — Application web commune

L’outil n’est **pas** dans l’application Android et **pas** dans ce dépôt Git. Dossier : `annuaire-commune-web/` (à côté de `annuaire.mg/`).

Rôle : permettre à l’agent de **lire** les dossiers Firebase et de **valider / refuser** CIN et métiers. HTML / CSS / JS, sans framework.

**Pourquoi le web, et pas un troisième espace mobile**

- L’agent travaille sur un poste, pas dans la rue
- Le cours porte sur l’app Android (visiteur + prestataire)
- Firebase est déjà le point de vérité partagé

**Comment l’ouvrir**

Ne pas ouvrir en `file://` (Firebase bloque souvent). Depuis le dossier :

```bash
python -m http.server 8080
```

Puis [http://localhost:8080](http://localhost:8080).  
Démo : **0320000000** / **agent123**.

L’URL Firebase est dans `config.js` (même base que `annuaire.api.baseUrl`).

#### Connexion

Carte de login : téléphone, mot de passe, rappel que cet outil n’est pas l’app mobile.

#### Tableau de bord

Après connexion :

- compteurs : dossiers CIN en attente, métiers à valider, dossiers déjà tranchés ;
- indicateur en ligne / hors ligne ;
- bouton Actualiser / Déconnexion.

#### Dossiers CIN

Liste à gauche, détail à droite :

- identité du prestataire, numéro CIN ;
- photos **recto** et **verso** (`cinRectoUrl`, `cinVersoUrl`) ;
- boutons **Valider** / **Refuser** → écriture sur `dossiers/{id}`.

Sans règles Firebase publiées (annexe C), la lecture marche, l’écriture affiche *Permission denied*.

#### Métiers proposés

Liste des noms `PENDING`. Valider rend le métier visible dans l’annuaire à la prochaine sync ; refuser le masque.

#### Chaîne avec l’app

1. Prestataire (Android) envoie le CIN → `dossiers/{id}` en `PENDING`
2. Agent (web) tranche → `CERTIFIED` ou `REJECTED`
3. Téléphone synchronise → le visiteur voit **Certifié**

Idem pour `metiers_proposes/{id}`.

---

### Annexe B — Comptes de démonstration

| Espace | Téléphone | Mot de passe | Où |
|--------|-----------|--------------|-----|
| Prestataire | 0341111111 | demo123 | App Android |
| Agent communal | 0320000000 | agent123 | Web uniquement |

Le compte agent est **refusé** dans l’app.

---

### Annexe C — Règles Firebase

À coller dans Realtime Database → Règles → **Publier** :

```json
{
  "rules": {
    ".read": true,
    "photos": { ".write": true },
    "dossiers": { ".write": true },
    "metiers_proposes": { ".write": true },
    "prestataires": { ".write": true },
    ".write": false
  }
}
```

Sans écriture sur `dossiers` / `metiers_proposes`, l’outil commune ne peut pas valider.

Import du catalogue : `remote-api/catalog.json` (nœud `catalog` ou racine). Détail des étapes : [`SYNC.md`](../SYNC.md).

---

### Annexe D — Correspondance avec le cours

| Notion | Dans le produit |
|--------|-----------------|
| Kotlin | Entités, statuts, null-safety |
| Jetpack Compose | Accueil, fiche, compte, espace prestataire |
| Navigation Compose | Liste → détail ; compte → prestataire |
| MVVM | `SearchViewModel` / `DetailViewModel` + Repository |
| Coroutines / Flow | Listes, session, sync |
| Room offline-first | Catalogue local, écritures prestataire |
| Intents | Appel, SMS, choix de photo |
| Réseau | Retrofit, réglage Wi‑Fi / mobile |

La commune web **ne remplace pas** ces notions : elle évite un troisième rôle dans le téléphone tout en fermant la boucle Certifié.

---

*Document de présentation — 10 septembre 2026. Plan aligné sur le livrable type « Projet transversal ». L’application web est volontairement en annexe.*

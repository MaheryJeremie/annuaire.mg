# Annuaire.mg — Contexte et comportement final

Cadrage du projet, versionné dans ce dépôt : [`docs/Annuaire_mg.md`](Annuaire_mg.md).  
Document de présentation (plan projet transversal) : [`Documentation.md`](Documentation.md).  
Dépôt GitHub : [github.com/MaheryJeremie/annuaire.mg](https://github.com/MaheryJeremie/annuaire.mg).  
Guide technique (Firebase, règles, sync) : [`SYNC.md`](../SYNC.md).

---

## 1. En une phrase

**annuaire.mg** est l’annuaire public des prestataires de proximité à Madagascar : un visiteur cherche par **métier et quartier**, voit **tarifs, disponibilité et avis**, puis **appelle** ou envoie un **SMS**. Les profils **certifiés** ont été contrôlés par la commune (CIN), via un outil web — pas dans l’application mobile.

Ce n’est pas une marketplace privée type Facebook : c’est un **registre à caractère communal**.

---

## 2. Nom et positionnement

| Élément | Valeur |
|--------|--------|
| **Nom** | annuaire.mg |
| **Type** | Annuaire public / e-gouvernement de proximité |
| **Plateformes** | App Android (visiteur + prestataire) + outil web commune |
| **Public** | Citoyens (sans compte) et prestataires ; agents communaux sur le web |
| **Contexte** | Projet M1 développement mobile |
| **GitHub** | https://github.com/MaheryJeremie/annuaire.mg |

### Ce que c’est
- Un **registre consultable** sans inscription
- Recherche **métier + quartier** (suggestions), filtres certifiés / dispo
- **Tarifs** (plusieurs lignes possibles), **disponibilité**, **avis**
- **Badge « Certifié »** après validation communale du CIN

### Ce que ce n’est PAS
- Pas un clone Facebook, pas d’Uber, pas de chat, pas de paiement in-app

---

## 3. Le problème

À Madagascar, pour trouver un service specifique, les gens postent souvent dans un **groupe Facebook**, attendent des réponses, demandent le prix en MP, et espèrent que la personne soit joignable.

| Problème | Effet |
|----------|--------|
| Info dispersée | Les posts disparaissent |
| Prix opaques | Perte de temps |
| Pas de structure métier / quartier | Impossible de vraiment chercher |
| Confiance faible | Numéros morts, arnaques |
| Connexion irrégulière | Facebook peu pratique hors ligne |

**Thèse :** un annuaire officiel cherchable, tarifs visibles, profils validés, consultable hors ligne.

---

## 4. Comportement livré (deux espaces mobiles + web commune)

### Visiteur (app, sans compte)
1. Ouvre **Accueil** : la liste est déjà là
2. Filtre si besoin : **Certifiés**, **Dispo aujourd’hui**, métier, quartier
3. Ouvre une fiche : tarifs, avis, badge **Certifié** s’il y a lieu
4. **Appeler** ou **SMS** ; peut laisser un avis
5. Choisis le mode de **synchronisation** en ligne qu'il préfere : wifi uniquement , n'importe quel reseau 

### Prestataire (app, compte)
1. **Compte** → se connecter ou créer un compte  
2. Complète la fiche :
   - photo de profil
   - métier (suggestions, ou proposition à la commune)
   - commune
   - **quartiers en sélection multiple** (menu déroulant, pas une longue liste de puces)
   - **tarifs** : autant de lignes que besoin (nom + montant Ar) ;
   - disponibilité du jour
3. Si le métier n’existe pas : il le propose ; la commune valide le nom
4. Envoie le **CIN** (numéro + recto + verso)
5. Le badge **Certifié** apparaît après décision de la commune **et** une sync 

### Commune (application web séparée)
- Pas dans l’app
- Valide / refuse les dossiers CIN et les métiers **sur Firebase**

---

## 5. Rôles

| Rôle | Besoin | Où |
|------|--------|-----|
| **Visiteur** | Trouver un prestataire près de chez lui | App |
| **Prestataire** | Être trouvé, afficher tarifs / dispo, demander le badge | App |
| **Agent communal** | Contrôler CIN et noms de métiers | **Web uniquement** |

---

## 6. « Certifié » — définition MVP

Un prestataire est **certifié** s’il a :
- une fiche complète ;
- envoyé son **CIN** (numéro + photos) ;
- été **accepté par la commune** dans l’outil web.

Les visiteurs ne voient que le badge **Certifié**. Les photos CIN ne sont pas publiques : la commune les consulte dans l’outil web.  
« Certifié » = recensé + CIN contrôlé, pas un agrément ministériel réel.

---

## 7. Périmètre livré

### Inclus
- Recherche liste-first, métier + quartier, certifiés, dispo ; **tirer vers le bas** pour actualiser
- Fiche publique : tarifs, avis, appel / SMS (Intents)
- Espace prestataire : photo, métier, **quartiers multi-select**, **plusieurs tarifs**, CIN
- Proposition de métier
- Offline-first Room ; sync catalogue selon le réglage **Compte** (Wi‑Fi par défaut)
- Back-office commune HTML + Firebase (`dossiers`, `metiers_proposes`)

### Exclus
- Paiement, messagerie, GPS temps réel, carte complexe

---

## 8. Écrans de l’app Android

1. **Accueil** — liste + filtres + tirer pour actualiser  
2. **Détail** — fiche, avis, appel / SMS  
3. **Aide**  
4. **Compte** — visiteur, connexion prestataire, réglage réseau  
5. **Espace prestataire** — fiche, CIN  

Navigation : **liste → détail**. Pas d’écran de validation communale.

L’outil commune (login, listes CIN / métiers, valider / refuser) est une **autre application**, web.

### Fiche prestataire (détail UX)

| Champ | Comportement |
|-------|----------------|
| Photo | Publique, distincte du CIN |
| Métier | Champ avec suggestions ; proposition si le nom n’existe pas |
| Commune | Liste déroulante simple |
| Quartiers | **Select multiple** : un menu, cases à cocher, puces uniquement pour les quartiers choisis |
| Tarifs | Une ou plusieurs lignes **nom + montant (Ar)** ; bouton **+ / Ajouter un tarif** ; une ligne se retire si ce n’est pas la dernière |
| Dispo | Interrupteur « Disponible aujourd’hui » |

---

## 9. Stack

| Couche | Technologie |
|--------|-------------|
| Langage | **Kotlin** |
| UI | **Jetpack Compose** |
| Navigation | **Navigation Compose** |
| Architecture | **MVVM** |
| Asynchrone | **Coroutines** + **Flow** |
| Local | **Room** = source de vérité |
| Réseau | **Retrofit + Moshi** (Firebase RTDB REST), sync selon réglage (Wi‑Fi par défaut) |
| Photos | Profil public (`photos/{id}`) ; CIN réservées à l’outil commune (`dossiers`) |
| Système | **Intents** (appel, SMS) |
| Commune | HTML / CSS / JS + Firebase REST |

```
UI Compose → ViewModel → Repository → Room
                              ↕ sync (Wi‑Fi par défaut)
                         Firebase (catalogue, dossiers, photos)
                              ↕
                    Outil web commune (hors app)
```

| Notion de cours | Dans le produit |
|----------------|-----------------|
| Kotlin | Entités, null-safety |
| Coroutines / Flow | Listes réactives, sync |
| Intents | Appel, SMS |
| Compose | Accueil, fiche, compte, espace prestataire |
| Navigation | Accueil → détail ; compte → prestataire |
| MVVM | UI / logique / données |
| Room offline-first | Annuaire hors ligne ; sync quand le réseau est autorisé |

La commune web ne remplace pas ces notions : elle évite un troisième rôle dans le téléphone.

---

## 10. Données

| Entité | Rôle |
|--------|------|
| `Metier` | Noms validés ou `PENDING` (proposés) |
| `Commune` / `Quartier` | Géographie (filtre visiteur = quartier, pas commune) |
| `Prestataire` | Fiche, CIN, `certificationStatus` |
| `Tarif` | Plusieurs par prestataire : libellé + Ariary |
| `Avis` | Note 1–5 |
| `User` | Prestataire uniquement dans l’app |

Firebase : `catalog` ou racine, `dossiers/{id}`, `metiers_proposes/{id}`, `photos/{id}`.

Démo prestataire : **0341111111** / **demo123**.  
Démo commune (web) : **0320000000** / **agent123**.

---

## 11. Phrase de présentation

> Les Malgaches cherchent souvent un artisan via Facebook : c’est lent, le prix est caché, on ne sait pas qui est dispo dans quel quartier.  
> **annuaire.mg** : tu parcours la liste, tu filtres métier ou quartier, tu vois tarifs, avis et le badge Certifié, tu appelles.  
> L’app mobile est pour le visiteur et le prestataire. La commune valide les CIN sur un petit outil web branché à Firebase.  
> Kotlin, Compose, Navigation, MVVM, Room offline-first, sync Wi‑Fi.

---

## 12. Où est le code

```text
Projet/
  annuaire.mg/                      App Android (ce dépôt Git)
    docs/Annuaire_mg.md             ← ce cadrage
    docs/Documentation.md           ← présentation (plan projet transversal)
    SYNC.md                         Firebase, règles, sync, photos
    app/                            Kotlin / Compose
  annuaire-commune-web/             Outil commune (hors Git)
```

Repo public : https://github.com/MaheryJeremie/annuaire.mg  

Branches habituelles : travail sur `feat/…` → PR vers `dev` → si OK, PR vers `main`.

`local.properties` (URL Firebase, Cloudinary) n’est **pas** commité.

---

## 13. Décisions figées

1. Recherche prestataire par métier / quartier, tarifs et dispo  
2. Angle **annuaire officiel**, pas app privée  
3. **Deux espaces dans l’app** : visiteur + prestataire  
4. **Commune = web Firebase**, pas d’écrans agent dans Android  
5. Stack Kotlin / Compose / MVVM / Room ; sync Wi‑Fi par défaut, réglable  
6. Badge visiteur = uniquement **Certifié**  
7. Photos CIN en ligne pour la commune seulement ; photo de profil sur la fiche publique  
8. Quartiers prestataire = **sélection multiple compacte**  
9. Tarifs prestataire = **plusieurs lignes**, ajout par bouton **+**

---

*Dernière mise à jour : 10 septembre 2026 — fiche prestataire (quartiers multi-select, tarifs multiples) et cadrage versionné dans `docs/`.*

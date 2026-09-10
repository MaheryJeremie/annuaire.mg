# annuaire.mg — sync, Firebase et rôles

Cadrage produit : [`docs/Annuaire_mg.md`](docs/Annuaire_mg.md).

## Comportement final

L’application **Android** n’a que **deux espaces**. La commune n’est **pas** dans l’app.

| Espace | Qui | Où |
|--------|-----|----|
| **Visiteur** | Tout le monde, sans compte | App mobile |
| **Prestataire** | Fiche + envoi du CIN | App mobile |
| **Commune** | Valider CIN et métiers | Outil web séparé, hors Git |

Navigation mobile : **Accueil / Aide / Compte**.

### Visiteur
- Pas de compte. La liste s’affiche tout de suite.
- Filtres : **Certifiés**, **Dispo aujourd’hui**, métier et quartier (suggestions). Pas de filtre commune.
- Badge public : uniquement **« Certifié »**. Pas d’« en attente » ni de « refusé » sur les fiches publiques.
- Appel et SMS (Intents). Avis (note 1–5 + commentaire).

### Prestataire
- Inscription / connexion dans **Compte**.
- Démo : **0341111111** / **demo123**.
- Fiche : photo, métier (suggestions ou nouveau nom), quartiers en **sélection multiple**, **plusieurs tarifs** (bouton +), disponibilité.
- Badge : envoi du **CIN** (numéro + photos recto/verso). Les photos partent en ligne pour la commune ; les visiteurs ne les voient pas.
- Un agent qui tente de se connecter dans l’app est renvoyé vers l’outil web.

### Commune (web uniquement)
- Dossier : `../annuaire-commune-web/` (pas dans ce dépôt Git).
- Démo : **0320000000** / **agent123**.
- Valide ou refuse les CIN et les métiers proposés **en ligne (Firebase)**.
- Aucun écran commune dans l’app Android.

### Comment le badge arrive dans l’app
1. Le prestataire envoie son CIN → Room (PENDING) + nœud Firebase `dossiers/{id}`.
2. La commune valide dans l’outil web → Firebase `dossiers/{id}` = `CERTIFIED` (ou `REJECTED`).
3. À la **prochaine sync** (Wi‑Fi par défaut, réglable dans Compte), l’app lit `dossiers` et affiche **Certifié**.

Même logique pour un **métier proposé** → `metiers_proposes/{id}`.

---

## Sync catalogue (réglage dans Compte)

Par défaut, la sync **réseau** ne part **que si le téléphone est en Wi‑Fi**.  
Dans **Compte → Synchronisation**, on peut choisir :

- **Wi‑Fi uniquement** (défaut)
- **Données mobiles uniquement**
- **N’importe quel réseau**

Sinon (réseau non autorisé, hors ligne, ou API en erreur) → `app/src/main/assets/catalog.json`.

L’app lit d’abord `{baseUrl}catalog.json`. Si ce nœud est vide, elle lit la **racine** `{baseUrl}.json` (cas actuel de la base de démo).

Puis elle fusionne :
- `photos/{id}` → photos de profil
- `dossiers/{id}` → décisions CIN
- `metiers_proposes/{id}` → décisions métiers

---

## Étape 1 — Créer le projet Firebase

1. [https://console.firebase.google.com](https://console.firebase.google.com)
2. **Ajouter un projet** → nom ex. `annuaire-mg`
3. Désactive Google Analytics si tu veux aller plus vite → **Créer le projet**

## Étape 2 — Créer Realtime Database

1. Menu gauche → **Realtime Database**
2. Région (ex. `europe-west1`) → **Suivant**
3. Mode **test** (MVP académique) → **Activer**

## Étape 3 — Importer le catalogue

Fichier : `remote-api/catalog.json`

Deux façons valides :

### Option A — nœud `catalog`
1. Crée un nœud `catalog`
2. Importe le JSON **à l’intérieur** (enfants : `version`, `communes`, `quartiers`, `prestataires`…)
3. URL : `{baseUrl}catalog.json`

### Option B — racine
Importe le JSON à la racine `/`. L’app lit alors `{baseUrl}.json`.

## Étape 4 — Règles (obligatoire pour la commune)

Realtime Database → **Règles** → coller → **Publier** :

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

Sans écriture sur `dossiers` / `metiers_proposes`, l’outil commune **lit** mais ne peut pas **Valider / Refuser**.

## Étape 5 — URL

```text
https://annuaire-mg-default-rtdb.europe-west1.firebasedatabase.app/
```

## Étape 6 — Android Studio

Dans `local.properties` (ne pas committer ce fichier) :

```properties
annuaire.api.baseUrl=https://annuaire-mg-default-rtdb.europe-west1.firebasedatabase.app/
```

**File → Sync Project with Gradle Files**, puis relancer l’app.

## Étape 7 — Tester la sync

| Situation | Attendu |
|-----------|---------|
| Wi‑Fi + Firebase OK | Catalogue distant + décisions `dossiers` |
| Données mobiles seulement | Catalogue `assets` |
| Wi‑Fi + URL fausse | Catalogue `assets` |

Pas de bouton de sync dans l’accueil : la sync part au lancement. Changer le réglage réseau dans Compte relance une sync. Après une validation commune, reconnecter selon le réglage choisi (Wi‑Fi par défaut).

Vérifier dans le navigateur `{baseUrl}.json` ou `{baseUrl}catalog.json`.

Pas besoin de `google-services.json` : REST Retrofit, pas le SDK Firebase Android.

---

## Photos (profil et CIN)

Les photos de **profil** : envoi distant, URL dans RTDB `photos/{id}`. Visibles dans l’annuaire.

Les photos **CIN** : envoi distant aussi, mais uniquement dans `dossiers/{id}` (`cinRectoUrl`, `cinVersoUrl`). L’outil commune les affiche. Les visiteurs ne les voient jamais. L’interface ne parle pas de CDN.

```properties
annuaire.cdn.cloudinaryCloud=VOTRE_CLOUD_NAME
annuaire.cdn.cloudinaryPreset=VOTRE_PRESET_UNSIGNED
```

---

## Outil commune (hors ce dépôt)

```text
Projet/
  annuaire.mg/              ← app Android (ce repo Git)
  annuaire-commune-web/     ← back-office HTML (hors Git)
```

1. Dans `annuaire-commune-web` : `python -m http.server 8080`
2. [http://localhost:8080](http://localhost:8080) (éviter `file://`)
3. **0320000000** / **agent123**
4. Règles Firebase déjà publiées (étape 4)

| Nœud Firebase | Rôle |
|---------------|------|
| `dossiers/{id}` | Acceptation / refus CIN + URLs des photos recto/verso |
| `metiers_proposes/{id}` | Acceptation / refus métier |
| `prestataires/{index}` | Statut dans le catalogue (si les règles l’autorisent) |

URL de l’outil : `annuaire-commune-web/config.js` (même base que `annuaire.api.baseUrl`).

---

## Fichiers utiles

| Fichier | Rôle |
|---------|------|
| `docs/Annuaire_mg.md` | Cadrage produit (espaces, UX, stack, démos) |
| `remote-api/catalog.json` | À importer dans Firebase |
| `app/src/main/assets/catalog.json` | Fallback local |
| `local.properties` | `annuaire.api.baseUrl` + Cloudinary |
| `WifiChecker.kt` | Autorise le réseau selon le réglage Compte (Wi‑Fi par défaut) |

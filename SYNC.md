# Guide Firebase + sync Wi‑Fi — annuaire.mg

## Règle dans l’app
La sync **réseau** ne part **que si le téléphone est en Wi‑Fi**.  
Sinon (4G/5G, hors ligne, ou API en erreur) → catalogue local `assets/catalog.json`.

Message possible dans la barre d’accueil :
- `Wi‑Fi / Firebase` → sync réussie
- `assets (hors Wi‑Fi)` → pas de Wi‑Fi, données locales
- `assets (Wi‑Fi mais API KO)` → Wi‑Fi OK mais Firebase pas encore branché / URL fausse

---

## Étape 1 — Créer le projet Firebase

1. Va sur [https://console.firebase.google.com](https://console.firebase.google.com)
2. **Ajouter un projet** → nom ex. `annuaire-mg`
3. Désactive Google Analytics si tu veux aller plus vite → **Créer le projet**

## Étape 2 — Créer Realtime Database

1. Menu gauche → **Create** → **Realtime Database**
2. Choisis une région (ex. `europe-west1`) → **Suivant**
3. Mode **démarrage en mode test** (OK pour MVP académique) → **Activer**

## Étape 3 — Importer le catalogue

1. Ouvre le fichier du projet :  
   `annuaire.mg/remote-api/catalog.json`
2. Dans Firebase RTDB → onglet **Données**
3. Sur la racine `null` / `/` → menu **⋮** → **Importer JSON**
4. Important : le fichier doit être importé pour que l’URL finale soit :

```text
https://VOTRE-PROJET-default-rtdb.REGION.firebasedatabase.app/catalog.json
```

**Deux façons valides :**

### Option A (recommandée — simple)
1. Crée un nœud nommé `catalog`
2. Importe le JSON **à l’intérieur** de `catalog`  
   (le contenu du fichier = enfants de `catalog` : `version`, `communes`, `quartiers`, …)

L’app appelle déjà `{baseUrl}catalog.json` → parfait.

### Option B
Héberge `catalog.json` sur **Firebase Hosting** (fichier statique) et pointe `baseUrl` vers ton site Hosting.

## Étape 4 — Règles de sécurité (MVP lecture seule)

Onglet **Règles** :

```json
{
  "rules": {
    ".read": true,
    "photos": {
      ".write": true
    },
    ".write": false
  }
}
```

→ **Publier**.  
(Plus tard : restreindre `.write` aux agents authentifiés.)

## Étape 5 — Récupérer l’URL

Dans Realtime Database, en haut, copie l’URL du type :

```text
https://annuaire-mg-default-rtdb.europe-west1.firebasedatabase.app/
```

## Étape 6 — Brancher Android Studio

1. Ouvre `annuaire.mg/local.properties`
2. Ajoute **une ligne** (avec ton URL réelle, slash final) :

```properties
annuaire.api.baseUrl=https://annuaire-mg-default-rtdb.europe-west1.firebasedatabase.app/
```

3. **File → Sync Project with Gradle Files**
4. Relance l’app (**Run**)

## Étape 7 — Tester

| Situation | Attendu |
|-----------|---------|
| Wi‑Fi ON + Firebase OK | Barre : `Catalogue Wi‑Fi / Firebase · …` |
| Données mobiles seulement | `Catalogue assets (hors Wi‑Fi) · …` |
| Wi‑Fi ON mais mauvaise URL | `Catalogue assets (Wi‑Fi mais API KO) · …` |

Bouton **⟳** sur l’accueil = relancer la sync.

---

## Vérifier l’URL dans le navigateur

Ouvre :

```text
https://VOTRE-PROJET-default-rtdb.REGION.firebasedatabase.app/catalog.json
```

Tu dois voir du JSON (`version`, `communes`, `quartiers`…).  
Si erreur 404 / permission : revoir nœud `catalog` et règles.

---

## Pas besoin de `google-services.json` pour ce MVP

Ici on utilise l’**API REST** de Realtime Database (Retrofit + HTTPS), pas le SDK Firebase complet.  
Donc **pas** d’ajout d’app Android Firebase obligatoire pour la sync catalogue.

---

## Fichiers utiles dans le projet

| Fichier | Rôle |
|---------|------|
| `remote-api/catalog.json` | À importer dans Firebase |
| `app/src/main/assets/catalog.json` | Fallback local |
| `local.properties` → `annuaire.api.baseUrl` | URL de ta base |
| `WifiChecker.kt` | Autorise sync uniquement en Wi‑Fi |

---

## Photos de profil (Cloudinary, offre gratuite)

Les photos **CIN** restent sur le téléphone.  
Les photos de **profil** sont envoyées vers Cloudinary, puis l’URL est indexée dans RTDB `photos/{id}`. Rien de tout cela n’apparaît dans l’interface.

1. Crée un compte gratuit : [https://cloudinary.com](https://cloudinary.com)
2. Dashboard → **Cloud name** (ex. `dxxxx`)
3. **Settings → Upload → Upload presets** → **Add upload preset**
   - Signing mode : **Unsigned**
   - **Save**
4. Dans `local.properties` :

```properties
annuaire.cdn.cloudinaryCloud=VOTRE_CLOUD_NAME
annuaire.cdn.cloudinaryPreset=VOTRE_PRESET_UNSIGNED
```

5. **File → Sync Project with Gradle Files**, puis relance l’app.

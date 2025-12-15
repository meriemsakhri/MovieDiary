# 🎬 MovieDiary (Android - Java)

MovieDiary est une application Android de **journal de cinéma** : elle permet à chaque utilisateur de gérer sa **wishlist**, ses **films regardés**, de **noter** les films (rating) et d’avoir une **page d’accueil personnalisée** selon ses préférences de genres.

---

## ✅ Fonctionnalités

### 🔐 Authentification
- Inscription / Connexion
- Sessions utilisateur (SharedPreferences)
- Mot de passe stocké de manière sécurisée (**hash + salt**)

### 🏠 Home (Catalogue)
- Affichage des films sous forme de cartes (RecyclerView + CardView)
- Barre de recherche (titre, genre, description)
- Personnalisation selon les préférences :
  - Les genres préférés apparaissent en premier
- Indicateurs par utilisateur :
  - ✅ **WATCHED** : carte en gris + rating visible
  - ❤️ **WISHLIST** : icône “favori” visible

### 🎞️ Détails d’un film
- Poster, titre, genre, année, description
- Gestion du statut :
  - Ajouter / retirer de **WISHLIST**
  - Ajouter / retirer de **WATCHED**
- Rating autorisé **uniquement si le film est WATCHED**

### 📔 My Diary
- Deux sections :
  - ❤️ Wishlist
  - ✅ Watched (avec rating)
- Possibilité de supprimer un film de chaque section
- Possibilité de modifier la note pour les films regardés

### 👤 Profil
- Affichage : username, email, préférences
- Modification : username / email / préférences (genres)
- Déconnexion

---

## 🧠 Logique métier (Business logic)
- Chaque utilisateur a son propre journal :
  - une ligne par `(user_id, movie_id)` dans `user_movies`
- Le home est personnalisé : les films correspondant aux genres préférés sont affichés en premier.
- La note (rating) ne s’applique qu’aux films **WATCHED**.

---

## 🛠️ Technologies utilisées
- **Java**
- **Android SDK**
- **SQLite (SQLiteOpenHelper)**
- **RecyclerView / CardView**
- **Glide** (chargement d’images)
- **SharedPreferences** (SessionManager)
- **Sécurité** : Hash + Salt pour les mots de passe

---

## 🗂️ Base de données (SQLite)

### Tables
- `users` : infos utilisateur + préférences
- `movies` : catalogue des films
- `user_movies` : journal utilisateur (wishlist / watched + rating)

### Règles importantes
- `user_movies` contient une contrainte :
  - **UNIQUE(user_id, movie_id)** → une seule ligne par film et par utilisateur
- `status` :
  - `WISHLIST` ou `WATCHED`
- `rating` :
  - entier `0..5` (0 signifie "pas noté")

---

## 🚀 Installation / Lancement
1. Cloner le projet :
   ```bash
   git clone https://github.com/meriemsakhri/MovieDiary.git

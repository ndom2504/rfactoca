# RFacto Backend API

Backend API pour l'application RFacto Shipping, déployé sur Vercel avec base de données Neon PostgreSQL.

## Structure du projet

```
backend/
├── api/
│   ├── auth/
│   │   └── google.js          # Authentification Google avec BDD
│   ├── payments/
│   │   └── create-payment-intent.js  # Création PaymentIntent Stripe avec BDD
│   └── init-db.js             # Initialisation de la base de données
├── lib/
│   ├── db.js                  # Configuration PostgreSQL Neon
│   └── schema.sql             # Schéma de la base de données
├── package.json
├── vercel.json
├── .env.example
└── README.md
```

## Base de données Neon PostgreSQL

Le backend utilise Neon PostgreSQL pour stocker :
- **Users** : Utilisateurs authentifiés avec Google
- **Parcels** : Colis créés par les utilisateurs
- **Payments** : Paiements Stripe
- **Tracking** : Suivi des colis

### Initialisation de la base de données

Avant d'utiliser l'API, initialisez la base de données :

```bash
curl -X POST https://votre-projet.vercel.app/api/init-db
```

## Points de terminaison

### POST /api/auth/google
Authentification avec Google Sign-In et enregistrement en base de données.

**Corps de la requête :**
```json
{
  "email": "user@example.com",
  "fullName": "John Doe",
  "idToken": "google_id_token"
}
```

**Réponse :**
```json
{
  "status": "success",
  "token": "base64_token",
  "userId": 123,
  "role": "client",
  "email": "user@example.com"
}
```

### POST /api/payments/create-payment-intent
Création d'un PaymentIntent Stripe avec enregistrement en base de données.

**Corps de la requête :**
```json
{
  "parcelId": 123,
  "amountCad": 25.50,
  "currency": "CAD",
  "userId": 456
}
```

**Réponse :**
```json
{
  "paymentIntentClientSecret": "pi_123_secret_abc",
  "ephemeralKeySecret": "ek_123_secret_xyz",
  "customerId": "cus_123456",
  "publishableKey": "pk_test_..."
}
```

### POST /api/init-db
Initialisation des tables de la base de données.

**Réponse :**
```json
{
  "success": true,
  "message": "Database initialized successfully"
}
```

## Configuration des variables d'environnement

Configurez ces variables dans votre projet Vercel :

1. **DATABASE_URL** : URL de connexion Neon PostgreSQL (format : `postgresql://user:password@host/database?sslmode=require`)
2. **STRIPE_SECRET_KEY** : Clé secrète Stripe (commence par `sk_test_` ou `sk_live_`)
3. **STRIPE_PUBLISHABLE_KEY** : Clé publique Stripe (commence par `pk_test_` ou `pk_live_`)
4. **GOOGLE_WEB_CLIENT_ID** : Client ID OAuth 2.0 de Google

## Configuration Neon PostgreSQL

1. Créez un projet sur [Neon](https://neon.tech)
2. Copiez la chaîne de connexion depuis le dashboard Neon
3. Ajoutez `DATABASE_URL` dans les variables d'environnement Vercel

## Déploiement sur Vercel

### Méthode 1 : Via l'interface web

1. Allez sur [vercel.com](https://vercel.com)
2. Importez votre projet GitHub
3. Configurez le **Root Directory** sur `backend`
4. Ajoutez les variables d'environnement
5. Déployez
6. Initialisez la base de données avec `POST /api/init-db`

### Méthode 2 : Via CLI

```bash
# Installer Vercel CLI
npm i -g vercel

# Se connecter
vercel login

# Déployer depuis le dossier backend
cd backend
vercel

# Pour la production
vercel --prod
```

## Développement local

```bash
cd backend
npm install
vercel dev
```

L'API sera disponible sur `http://localhost:3000`

## Mise à jour de l'application Android

Une fois déployé, mettez à jour le fichier `.env` dans votre projet Android :

```env
REMOTE_API_URL=https://votre-projet.vercel.app
```

## Notes importantes

- L'authentification Google utilise maintenant la base de données Neon pour gérer les utilisateurs
- Le backend réutilise les clients Stripe existants pour les utilisateurs
- Assurez-vous que votre domaine Vercel est autorisé dans votre console Google Cloud et Stripe
- Initialisez la base de données avant d'utiliser l'API en production

-- Create users table
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  google_id VARCHAR(255) UNIQUE,
  stripe_customer_id VARCHAR(255),
  role VARCHAR(50) DEFAULT 'client',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create parcels table
CREATE TABLE IF NOT EXISTS parcels (
  id SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users(id),
  numero VARCHAR(50) UNIQUE NOT NULL,
  nom VARCHAR(255) NOT NULL,
  description TEXT,
  poids_est DECIMAL(10, 2),
  dimensions VARCHAR(100),
  valeur DECIMAL(10, 2),
  pays_dest VARCHAR(100),
  ville VARCHAR(100),
  adresse TEXT,
  mode_livraison VARCHAR(50),
  photo_url TEXT,
  statut VARCHAR(50) DEFAULT 'EN_ATTENTE',
  prix_estime DECIMAL(10, 2),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
  id SERIAL PRIMARY KEY,
  parcel_id INTEGER REFERENCES parcels(id),
  user_id INTEGER REFERENCES users(id),
  amount DECIMAL(10, 2) NOT NULL,
  currency VARCHAR(10) DEFAULT 'CAD',
  payment_method VARCHAR(50),
  stripe_payment_intent_id VARCHAR(255),
  stripe_customer_id VARCHAR(255),
  statut VARCHAR(50) DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create tracking table
CREATE TABLE IF NOT EXISTS tracking (
  id SERIAL PRIMARY KEY,
  parcel_id INTEGER REFERENCES parcels(id),
  statut VARCHAR(100) NOT NULL,
  location VARCHAR(255),
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id);
CREATE INDEX IF NOT EXISTS idx_parcels_user_id ON parcels(user_id);
CREATE INDEX IF NOT EXISTS idx_parcels_numero ON parcels(numero);
CREATE INDEX IF NOT EXISTS idx_parcels_statut ON parcels(statut);
CREATE INDEX IF NOT EXISTS idx_payments_parcel_id ON payments(parcel_id);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_tracking_parcel_id ON tracking(parcel_id);

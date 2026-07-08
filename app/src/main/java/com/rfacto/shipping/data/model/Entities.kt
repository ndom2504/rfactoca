package com.rfacto.shipping.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utilisateurs")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nom: String,
    val prenom: String,
    val email: String,
    val telephone: String,
    val motDePasse: String,
    val role: String, // "CLIENT", "AGENT_CANADA", "AGENT_LOCAL", "ADMIN"
    val pays: String,
    val statut: String, // "ACTIVE", "PENDING"
    val profilePhoto: String? = null,
    val ville: String? = null,
    val adresse: String? = null,
    val prefLangue: String = "Français",
    val prefNotifSms: Boolean = true,
    val prefNotifEmail: Boolean = true,
    val prefNotifPush: Boolean = true
)

@Entity(tableName = "colis")
data class Colis(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numero: String, // RFC-2026-XXXXXX
    val clientId: Int,
    val clientName: String, // Cache client name for agent display
    val description: String,
    val poids: Double, // Real or estimated
    val dimensions: String, // Real or estimated, e.g. "30x20x15"
    val valeur: Double,
    val photo: String?, // String description/uri of photo
    val paysDestination: String,
    val ville: String,
    val adresseDestination: String,
    val modeLivraison: String, // "POINT_RELAIS", "LIVRAISON_DOMICILE"
    val statut: String, // "CREE", "EN_ATTENTE_RECEPTION", "RECU_CANADA", "PAIEMENT_VALIDE", "EN_PREPARATION", "EXPEDIE", "EN_DOUANE", "ARRIVE_PAYS", "CENTRE_LOCAL", "EN_LIVRAISON", "LIVRE"
    val dateCreation: Long = System.currentTimeMillis()
)

@Entity(tableName = "paiements")
data class Paiement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val colisId: Int,
    val colisNumero: String,
    val montant: Double,
    val mode: String, // "CARTE_BANCAIRE", "MOBILE_MONEY", "PAYPAL", "VIREMENT", "INTERAC"
    val statut: String, // "PENDING", "PAID", "FAILED"
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "suivi")
data class Suivi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val colisId: Int,
    val statut: String,
    val lieu: String,
    val date: Long = System.currentTimeMillis(),
    val commentaire: String
)

@Entity(tableName = "agences")
data class Agence(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nom: String,
    val pays: String,
    val ville: String,
    val adresse: String
)

@Entity(tableName = "agents")
data class Agent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nom: String,
    val telephone: String,
    val agenceId: Int
)

@Entity(tableName = "tarifs")
data class Tarifs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pays: String,
    val prixParKg: Double,
    val livraisonLocale: Double,
    val assurance: Double
)

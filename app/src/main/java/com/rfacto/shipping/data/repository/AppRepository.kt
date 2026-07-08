package com.rfacto.shipping.data.repository

import com.rfacto.shipping.data.dao.*
import com.rfacto.shipping.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AppRepository(
    private val userDao: UserDao,
    private val colisDao: ColisDao,
    private val paiementDao: PaiementDao,
    private val suiviDao: SuiviDao,
    private val agenceDao: AgenceDao,
    private val agentDao: AgentDao,
    private val tarifsDao: TarifsDao
) {
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val allColis: Flow<List<Colis>> = colisDao.getAllColis()
    val allPaiements: Flow<List<Paiement>> = paiementDao.getAllPaiements()
    val allAgences: Flow<List<Agence>> = agenceDao.getAllAgences()
    val allAgents: Flow<List<Agent>> = agentDao.getAllAgents()
    val allTarifs: Flow<List<Tarifs>> = tarifsDao.getAllTarifs()

    fun getColisForClient(clientId: Int): Flow<List<Colis>> = colisDao.getColisForClient(clientId)
    fun getSuiviForColis(colisId: Int): Flow<List<Suivi>> = suiviDao.getSuiviForColis(colisId)
    fun getPaiementsForColis(colisId: Int): Flow<List<Paiement>> = paiementDao.getPaiementsForColis(colisId)

    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun getUserByPhone(phone: String): User? = userDao.getUserByPhone(phone)
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun getColisById(id: Int): Colis? = colisDao.getColisById(id)
    suspend fun getColisByNumero(numero: String): Colis? = colisDao.getColisByNumero(numero)
    suspend fun insertColis(colis: Colis): Long = colisDao.insertColis(colis)
    suspend fun updateColis(colis: Colis) = colisDao.updateColis(colis)
    suspend fun deleteColis(id: Int) = colisDao.deleteColis(id)

    suspend fun insertPaiement(paiement: Paiement): Long = paiementDao.insertPaiement(paiement)
    suspend fun updatePaiement(paiement: Paiement) = paiementDao.updatePaiement(paiement)

    suspend fun insertSuivi(suivi: Suivi): Long = suiviDao.insertSuivi(suivi)

    suspend fun insertAgence(agence: Agence): Long = agenceDao.insertAgence(agence)
    suspend fun insertAgent(agent: Agent): Long = agentDao.insertAgent(agent)

    suspend fun getTarifByPays(pays: String): Tarifs? = tarifsDao.getTarifByPays(pays)
    suspend fun insertTarif(tarifs: Tarifs): Long = tarifsDao.insertTarif(tarifs)
    suspend fun updateTarif(tarifs: Tarifs) = tarifsDao.updateTarif(tarifs)

    suspend fun initializeSeedData() {
        val existingUsers = allUsers.first()
        if (existingUsers.isNotEmpty()) {
            return // Database already has data
        }

        // 1. Seed Tarifs
        val gabonTarif = Tarifs(pays = "Gabon", prixParKg = 15.0, livraisonLocale = 8.0, assurance = 5.0)
        val franceTarif = Tarifs(pays = "France", prixParKg = 10.0, livraisonLocale = 5.0, assurance = 3.0)
        val canadaTarif = Tarifs(pays = "Canada", prixParKg = 5.0, livraisonLocale = 4.0, assurance = 2.0)

        insertTarif(gabonTarif)
        insertTarif(franceTarif)
        insertTarif(canadaTarif)

        // 2. Seed Agences
        val agenceMtlId = insertAgence(Agence(nom = "RFacto Transit Center Québec", pays = "Canada", ville = "Laurier-station", adresse = "127 rue Talbot, Laurier-station, Québec"))
        val agenceLbvId = insertAgence(Agence(nom = "RFacto Libreville Relay", pays = "Gabon", ville = "Libreville", adresse = "Quartier Glass, Libreville"))
        val agenceParId = insertAgence(Agence(nom = "RFacto Paris Relay", pays = "France", ville = "Paris", adresse = "Rue de la Pompe, Paris"))

        // 3. Seed Users
        // Default users to allow immediate logging in or switching roles easily:
        val client1Id = insertUser(User(
            nom = "Ndong", prenom = "Jean", email = "jean@rfacto.com", telephone = "+15814439464",
            motDePasse = "password123", role = "CLIENT", pays = "Gabon", statut = "ACTIVE"
        )).toInt()

        val agentCanadaId = insertUser(User(
            nom = "Tremblay", prenom = "Pierre", email = "canada@rfacto.com", telephone = "+15149876543",
            motDePasse = "password123", role = "AGENT_CANADA", pays = "Canada", statut = "ACTIVE"
        )).toInt()

        val agentLocalId = insertUser(User(
            nom = "Mba", prenom = "Charles", email = "local@rfacto.com", telephone = "+24166000000",
            motDePasse = "password123", role = "AGENT_LOCAL", pays = "Gabon", statut = "ACTIVE"
        )).toInt()

        val adminId = insertUser(User(
            nom = "SOP", prenom = "Marc", email = "admin@rfacto.com", telephone = "+15145550000",
            motDePasse = "password123", role = "ADMIN", pays = "Canada", statut = "ACTIVE"
        )).toInt()

        // 4. Seed Agents table
        insertAgent(Agent(nom = "Pierre Tremblay", telephone = "+15149876543", agenceId = agenceMtlId.toInt()))
        insertAgent(Agent(nom = "Charles Mba", telephone = "+24166000000", agenceId = agenceLbvId.toInt()))

        // 5. Seed Parcels (Colis)
        // Let's create some sample colis for client1 (Jean) to show a rich dashboard:
        
        // Colis 1: Arrived & Delivered
        val c1Id = insertColis(Colis(
            numero = "RFC-2026-000101", clientId = client1Id, clientName = "Jean Ndong",
            description = "Vêtements d'hiver et chaussures", poids = 4.5, dimensions = "30x20x20", valeur = 150.0,
            photo = null, paysDestination = "Gabon", ville = "Libreville", adresseDestination = "Quartier Glass, Libreville",
            modeLivraison = "POINT_RELAIS", statut = "LIVRE", dateCreation = System.currentTimeMillis() - 10 * 24 * 3600 * 1000
        )).toInt()
        insertPaiement(Paiement(colisId = c1Id, colisNumero = "RFC-2026-000101", montant = 75.5, mode = "CARTE_BANCAIRE", statut = "PAID"))
        insertSuivi(Suivi(colisId = c1Id, statut = "CREE", lieu = "Canada (Client)", commentaire = "Colis déclaré par le client"))
        insertSuivi(Suivi(colisId = c1Id, statut = "RECU_CANADA", lieu = "Laurier-station QC", commentaire = "Réceptionné au Transit Center Canada. Poids réel: 4.5kg"))
        insertSuivi(Suivi(colisId = c1Id, statut = "PAIEMENT_VALIDE", lieu = "Laurier-station QC", commentaire = "Paiement de 75.5$ validé"))
        insertSuivi(Suivi(colisId = c1Id, statut = "EXPEDIE", lieu = "Laurier-station QC", commentaire = "Expédié via vol Cargo RFacto #RFC109"))
        insertSuivi(Suivi(colisId = c1Id, statut = "ARRIVE_PAYS", lieu = "Libreville Airport", commentaire = "Colis arrivé au Gabon, dédouanement en cours"))
        insertSuivi(Suivi(colisId = c1Id, statut = "CENTRE_LOCAL", lieu = "Libreville Relay", commentaire = "Prêt pour retrait au point relais Libreville"))
        insertSuivi(Suivi(colisId = c1Id, statut = "LIVRE", lieu = "Libreville Relay", commentaire = "Remis en main propre au destinataire. Code de retrait valide."))

        // Colis 2: En Transit (Expédié)
        val c2Id = insertColis(Colis(
            numero = "RFC-2026-000125", clientId = client1Id, clientName = "Jean Ndong",
            description = "Ordinateur Portable ASUS", poids = 3.0, dimensions = "40x30x10", valeur = 800.0,
            photo = null, paysDestination = "Gabon", ville = "Port-Gentil", adresseDestination = "Château d'eau, Port-Gentil",
            modeLivraison = "LIVRE_DOMICILE", statut = "EXPEDIE", dateCreation = System.currentTimeMillis() - 4 * 24 * 3600 * 1000
        )).toInt()
        insertPaiement(Paiement(colisId = c2Id, colisNumero = "RFC-2026-000125", montant = 58.0, mode = "PAYPAL", statut = "PAID"))
        insertSuivi(Suivi(colisId = c2Id, statut = "CREE", lieu = "Canada (Client)", commentaire = "Colis déclaré par le client"))
        insertSuivi(Suivi(colisId = c2Id, statut = "RECU_CANADA", lieu = "Laurier-station QC", commentaire = "Réceptionné au Transit Center Canada"))
        insertSuivi(Suivi(colisId = c2Id, statut = "PAIEMENT_VALIDE", lieu = "Laurier-station QC", commentaire = "Paiement validé par PayPal"))
        insertSuivi(Suivi(colisId = c2Id, statut = "EXPEDIE", lieu = "Laurier-station QC", commentaire = "Expédié de Montréal vers Libreville"))

        // Colis 3: Reçu au Canada, Paiement en attente
        val c3Id = insertColis(Colis(
            numero = "RFC-2026-000142", clientId = client1Id, clientName = "Jean Ndong",
            description = "Téléphone Samsung Galaxy S24", poids = 0.8, dimensions = "15x10x5", valeur = 1000.0,
            photo = null, paysDestination = "France", ville = "Paris", adresseDestination = "Rue de la Pompe, Paris",
            modeLivraison = "POINT_RELAIS", statut = "RECU_CANADA", dateCreation = System.currentTimeMillis() - 2 * 24 * 3600 * 1000
        )).toInt()
        insertPaiement(Paiement(colisId = c3Id, colisNumero = "RFC-2026-000142", montant = 25.0, mode = "MOBILE_MONEY", statut = "PENDING"))
        insertSuivi(Suivi(colisId = c3Id, statut = "CREE", lieu = "Canada (Client)", commentaire = "Déclaré"))
        insertSuivi(Suivi(colisId = c3Id, statut = "RECU_CANADA", lieu = "Laurier-station QC", commentaire = "Réceptionné, pesé (0.8kg), en attente de paiement."))

        // Colis 4: Déclaré (En attente de réception au Canada)
        val c4Id = insertColis(Colis(
            numero = "RFC-2026-000189", clientId = client1Id, clientName = "Jean Ndong",
            description = "Livres d'études de médecine", poids = 12.0, dimensions = "50x40x30", valeur = 300.0,
            photo = null, paysDestination = "Gabon", ville = "Libreville", adresseDestination = "Point Relais Libreville Glass",
            modeLivraison = "POINT_RELAIS", statut = "CREE", dateCreation = System.currentTimeMillis() - 12 * 3600 * 1000
        )).toInt()
        insertSuivi(Suivi(colisId = c4Id, statut = "CREE", lieu = "Canada (Client)", commentaire = "Colis déclaré en ligne par Jean Ndong. En attente de livraison au dépôt de Laurier-station."))
    }
}

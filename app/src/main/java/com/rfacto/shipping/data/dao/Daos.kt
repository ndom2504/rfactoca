package com.rfacto.shipping.data.dao

import androidx.room.*
import com.rfacto.shipping.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM utilisateurs ORDER BY id DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM utilisateurs WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM utilisateurs WHERE telephone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface ColisDao {
    @Query("SELECT * FROM colis ORDER BY dateCreation DESC")
    fun getAllColis(): Flow<List<Colis>>

    @Query("SELECT * FROM colis WHERE clientId = :clientId ORDER BY dateCreation DESC")
    fun getColisForClient(clientId: Int): Flow<List<Colis>>

    @Query("SELECT * FROM colis WHERE id = :id LIMIT 1")
    suspend fun getColisById(id: Int): Colis?

    @Query("SELECT * FROM colis WHERE numero = :numero LIMIT 1")
    suspend fun getColisByNumero(numero: String): Colis?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColis(colis: Colis): Long

    @Update
    suspend fun updateColis(colis: Colis)

    @Query("DELETE FROM colis WHERE id = :id")
    suspend fun deleteColis(id: Int)
}

@Dao
interface PaiementDao {
    @Query("SELECT * FROM paiements ORDER BY date DESC")
    fun getAllPaiements(): Flow<List<Paiement>>

    @Query("SELECT * FROM paiements WHERE colisId = :colisId ORDER BY date DESC")
    fun getPaiementsForColis(colisId: Int): Flow<List<Paiement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaiement(paiement: Paiement): Long

    @Update
    suspend fun updatePaiement(paiement: Paiement)
}

@Dao
interface SuiviDao {
    @Query("SELECT * FROM suivi WHERE colisId = :colisId ORDER BY date ASC")
    fun getSuiviForColis(colisId: Int): Flow<List<Suivi>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuivi(suivi: Suivi): Long
}

@Dao
interface AgenceDao {
    @Query("SELECT * FROM agences ORDER BY nom ASC")
    fun getAllAgences(): Flow<List<Agence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgence(agence: Agence): Long
}

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents ORDER BY nom ASC")
    fun getAllAgents(): Flow<List<Agent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: Agent): Long
}

@Dao
interface TarifsDao {
    @Query("SELECT * FROM tarifs ORDER BY pays ASC")
    fun getAllTarifs(): Flow<List<Tarifs>>

    @Query("SELECT * FROM tarifs WHERE pays = :pays LIMIT 1")
    suspend fun getTarifByPays(pays: String): Tarifs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarif(tarifs: Tarifs): Long

    @Update
    suspend fun updateTarif(tarifs: Tarifs)
}

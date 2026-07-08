package com.rfacto.shipping.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rfacto.shipping.data.dao.*
import com.rfacto.shipping.data.model.*

@Database(
    entities = [
        User::class,
        Colis::class,
        Paiement::class,
        Suivi::class,
        Agence::class,
        Agent::class,
        Tarifs::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun colisDao(): ColisDao
    abstract fun paiementDao(): PaiementDao
    abstract fun suiviDao(): SuiviDao
    abstract fun agenceDao(): AgenceDao
    abstract fun agentDao(): AgentDao
    abstract fun tarifsDao(): TarifsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rfacto_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.cryptoexchange.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDao {

    @Query("SELECT * FROM crypto_currencies ORDER BY rank ASC")
    fun getTopCryptoList(): Flow<List<CryptoEntity>>

    @Query("DELETE FROM crypto_currencies")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsertAll(entities: List<CryptoEntity>)

    @Transaction
    suspend fun updateTop30(entities: List<CryptoEntity>) {
        deleteAll()
        upsertAll(entities)
    }
}

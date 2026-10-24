package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculation_history WHERE type = :type ORDER BY timestamp DESC")
    fun getRecordsByType(type: String): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculation_history WHERE expression LIKE '%' || :query || '%' OR result LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchRecords(query: String): Flow<List<CalculationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CalculationRecord): Long

    @Delete
    suspend fun deleteRecord(record: CalculationRecord)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()

    @Query("UPDATE calculation_history SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE calculation_history SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String?)
}

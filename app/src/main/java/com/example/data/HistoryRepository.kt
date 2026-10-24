package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {

    fun getAllRecords(): Flow<List<CalculationRecord>> = historyDao.getAllRecords()

    fun getRecordsByType(type: String): Flow<List<CalculationRecord>> =
        historyDao.getRecordsByType(type)

    fun searchRecords(query: String): Flow<List<CalculationRecord>> =
        historyDao.searchRecords(query)

    suspend fun saveRecord(record: CalculationRecord): Long =
        historyDao.insertRecord(record)

    suspend fun deleteRecord(record: CalculationRecord) =
        historyDao.deleteRecord(record)

    suspend fun deleteById(id: Long) =
        historyDao.deleteById(id)

    suspend fun clearAll() =
        historyDao.clearAll()

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) =
        historyDao.updateFavorite(id, isFavorite)

    suspend fun updateNote(id: Long, note: String?) =
        historyDao.updateNote(id, note)
}

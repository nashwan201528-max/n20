package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "CALCULATOR", "CURRENCY", "TAX", "LOAN", "DISCOUNT", "TIP", "MARGIN"
    val title: String,
    val expression: String,
    val result: String,
    val secondaryDetails: String = "",
    val note: String? = null,
    val isFavorite: Boolean = false
)

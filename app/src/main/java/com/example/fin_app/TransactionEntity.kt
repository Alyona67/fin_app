package com.example.fin_app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val categoryId: Int,
    val type: String,
    val date: Long,
    val goalName: String? = null
)
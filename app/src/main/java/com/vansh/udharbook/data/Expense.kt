package com.vansh.udharbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Int,
    val amount: Int,
    val category: String, // "General"
    val note: String,
    val type: String, // "IN" or "OUT" <--- NEW FIELD
    val timestamp: Long
)
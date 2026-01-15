package com.vansh.udharbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_table")
data class Business(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String = "General" // <--- NEW FIELD: "General" or "Dairy"
)
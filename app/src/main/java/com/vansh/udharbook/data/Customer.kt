package com.vansh.udharbook.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "customer_table",
    foreignKeys = [
        ForeignKey(
            entity = Business::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val businessId: Int, // <--- LINKS CUSTOMER TO A SPECIFIC BUSINESS
    val name: String,
    val mobile: String,
    val role: String, // "Customer" or "Supplier"
    val balance: Int = 0,
    val address: String = ""
)
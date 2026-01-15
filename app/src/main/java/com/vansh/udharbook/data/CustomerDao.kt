package com.vansh.udharbook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    // --- BUSINESS ---
    @Insert
    suspend fun insertBusiness(business: Business)

    @Query("SELECT * FROM business_table")
    fun getAllBusinesses(): Flow<List<Business>>

    @Update
    suspend fun updateBusiness(business: Business)

    @Delete
    suspend fun deleteBusiness(business: Business)

    // --- CUSTOMER ---
    @Query("SELECT * FROM customer_table WHERE businessId = :businessId ORDER BY id DESC")
    fun getCustomersForBusiness(businessId: Int): Flow<List<Customer>>

    @Insert
    suspend fun insert(customer: Customer)

    @Query("SELECT * FROM customer_table WHERE id = :id")
    fun getCustomer(id: Int): Flow<Customer?>

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customer_table SET balance = :newBalance WHERE id = :customerId")
    suspend fun updateCustomerBalance(customerId: Int, newBalance: Int)

    @Query("DELETE FROM customer_table WHERE id = :customerId")
    suspend fun deleteCustomer(customerId: Int)

    // --- TRANSACTION ---
    @Query("SELECT * FROM transaction_table WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: Int): Flow<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // --- NEW: EXPENSE COMMANDS ---
    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expense_table WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getExpensesForBusiness(businessId: Int): Flow<List<Expense>>

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
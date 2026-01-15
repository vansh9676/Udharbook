package com.vansh.udharbook.ui.theme

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vansh.udharbook.data.CustomerDao
import com.vansh.udharbook.data.Expense
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashbookScreen(
    navController: NavController,
    customerDao: CustomerDao,
    businessId: Int
) {
    val expenseList by customerDao.getExpensesForBusiness(businessId).collectAsState(initial = emptyList())

    // Calculations
    val totalIn = expenseList.filter { it.type == "IN" }.sumOf { it.amount }
    val totalOut = expenseList.filter { it.type == "OUT" }.sumOf { it.amount }
    val netBalance = totalIn - totalOut

    // Dialog States
    var showAddDialog by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf("OUT") } // "IN" or "OUT"

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Input States
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }

    fun formatDate(timestamp: Long): String = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cashbook", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UdharGreenPrimary)
            )
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                // RED BUTTON (Cash OUT)
                Button(
                    onClick = {
                        transactionType = "OUT"; amount = ""; note = ""; date = System.currentTimeMillis()
                        showAddDialog = true
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UdharRed),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("CASH OUT (-)", fontWeight = FontWeight.Bold) }

                Spacer(modifier = Modifier.width(16.dp))

                // GREEN BUTTON (Cash IN)
                Button(
                    onClick = {
                        transactionType = "IN"; amount = ""; note = ""; date = System.currentTimeMillis()
                        showAddDialog = true
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UdharGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("CASH IN (+)", fontWeight = FontWeight.Bold) }
            }
        },
        containerColor = UdharBackground
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // SUMMARY CARD
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Net Balance", color = Color.Gray, fontSize = 12.sp)
                    Text("₹ $netBalance", color = if (netBalance >= 0) UdharGreenPrimary else UdharRed, fontWeight = FontWeight.Bold, fontSize = 24.sp)

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total In (+)", color = UdharGreenPrimary, fontSize = 12.sp)
                            Text("₹ $totalIn", color = UdharGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Out (-)", color = UdharRed, fontSize = 12.sp)
                            Text("₹ $totalOut", color = UdharRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }

            // LIST
            if (expenseList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No cash entries yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(expenseList) { expense ->
                        CashbookItem(expense) {
                            scope.launch { customerDao.deleteExpense(expense) }
                        }
                    }
                }
            }
        }
    }

    // --- ADD DIALOG ---
    if (showAddDialog) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateString = sdf.format(Date(date))
        val isIncome = transactionType == "IN"
        val title = if (isIncome) "Add Cash In" else "Add Cash Out"
        val btnColor = if (isIncome) UdharGreenPrimary else UdharRed

        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                date = calendar.timeInMillis
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(title, color = btnColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = btnColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dateString, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Change", color = btnColor, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Remark (e.g. Sales, Rent)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountInt = amount.toIntOrNull()
                        if (amountInt != null && amountInt > 0) {
                            scope.launch {
                                customerDao.insertExpense(
                                    Expense(
                                        businessId = businessId,
                                        amount = amountInt,
                                        category = "General",
                                        note = note,
                                        type = transactionType, // "IN" or "OUT"
                                        timestamp = date
                                    )
                                )
                                showAddDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor)
                ) {
                    Text("SAVE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("CANCEL", color = Color.Gray) }
            }
        )
    }
}

@Composable
fun CashbookItem(expense: Expense, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    val dateStr = sdf.format(Date(expense.timestamp))
    val isIncome = expense.type == "IN"
    val color = if (isIncome) UdharGreenPrimary else UdharRed
    val prefix = if (isIncome) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp)) {
            Text(dateStr, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))

        Text(expense.note.ifEmpty { "No Remark" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        Text("$prefix ₹ ${expense.amount}", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
        }
    }
    Divider(color = Color.LightGray, thickness = 0.5.dp)
}
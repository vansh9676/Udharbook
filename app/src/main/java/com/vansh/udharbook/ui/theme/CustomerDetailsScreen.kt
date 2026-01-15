package com.vansh.udharbook.ui.theme

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vansh.udharbook.data.CustomerDao
import com.vansh.udharbook.data.Transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    navController: NavController,
    customerDao: CustomerDao,
    customerId: Int
) {
    val context = LocalContext.current
    val customerState by customerDao.getCustomer(customerId).collectAsState(initial = null)
    val transactionList by customerDao.getTransactionsForCustomer(customerId).collectAsState(initial = emptyList())

    // Business Category Logic
    var businessCategory by remember { mutableStateOf("General") }

    LaunchedEffect(customerState) {
        customerState?.let { cust ->
            val businesses = customerDao.getAllBusinesses().first()
            val myBusiness = businesses.find { it.id == cust.businessId }
            if (myBusiness != null) {
                businessCategory = myBusiness.category
            }
        }
    }

    // Dialog States
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Data holders
    var currentTransaction by remember { mutableStateOf<Transaction?>(null) }
    var dialogType by remember { mutableStateOf("GAVE") }
    var dialogAmountText by remember { mutableStateOf("") }
    var dialogNoteText by remember { mutableStateOf("") }
    var dialogDate by remember { mutableStateOf(System.currentTimeMillis()) }

    val scope = rememberCoroutineScope()
    val customer = customerState ?: return

    fun formatCurrency(amount: Int): String = "₹ ${amount.absoluteValue}"

    // --- HELPER FUNCTIONS ---
    fun makeCall(number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            context.startActivity(intent)
        } catch (e: Exception) { Toast.makeText(context, "Cannot open Dialer", Toast.LENGTH_SHORT).show() }
    }

    fun sendWhatsApp(number: String, message: String) {
        try {
            val cleanNumber = number.replace(" ", "").replace("+", "")
            val finalNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$finalNumber&text=${Uri.encode(message)}")
            context.startActivity(intent)
        } catch (e: Exception) { Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show() }
    }

    fun sendSMS(number: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null))
            intent.putExtra("sms_body", message)
            context.startActivity(intent)
        } catch (e: Exception) { Toast.makeText(context, "Cannot open SMS app", Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.clickable { navController.navigate("customer_profile/${customer.id}") }) {
                        Text(customer.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${customer.role} • View settings >", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { makeCall(customer.mobile) }) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UdharGreenPrimary)
            )
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                Button(
                    onClick = {
                        dialogType = "GAVE"; dialogAmountText = ""; dialogNoteText = ""; dialogDate = System.currentTimeMillis()
                        showAddDialog = true
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UdharRed),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("YOU GAVE ₹", fontWeight = FontWeight.Bold) }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        dialogType = "GOT"; dialogAmountText = ""; dialogNoteText = ""; dialogDate = System.currentTimeMillis()
                        showAddDialog = true
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UdharGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("YOU GOT ₹", fontWeight = FontWeight.Bold) }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(UdharBackground)) {

            // Balance Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(if (customer.balance >= 0) "You will get" else "You will give", color = UdharTextBlack, fontSize = 18.sp)
                        Text(formatCurrency(customer.balance), color = if (customer.balance >= 0) UdharGreenPrimary else UdharRed, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                }
            }

            // Communication Buttons
            // Communication Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val msg = "Hello ${customer.name}, your total pending balance is ${formatCurrency(customer.balance)}. Please pay at the earliest."

                // 1. WhatsApp Button (Unchanged)
                ActionButton(icon = Icons.Default.Chat, text = "WhatsApp", color = Color(0xFF25D366)) {
                    sendWhatsApp(customer.mobile, msg)
                }

                // 2. SMS Button (Unchanged)
                ActionButton(icon = Icons.Default.Message, text = "SMS", color = Color(0xFF03A9F4)) {
                    sendSMS(customer.mobile, msg)
                }

                // 3. REPORT BUTTON (Updated for PDF)
                ActionButton(icon = Icons.Default.Description, text = "Report", color = UdharGreenPrimary) {
                    if (transactionList.isNotEmpty()) {
                        // Triggers the PDF Generator we just created
                        PdfGenerator.generateAndShareReport(context, customer, transactionList)
                    } else {
                        Toast.makeText(context, "No transactions to report", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Transaction List
            TransactionHistoryList(
                transactionList = transactionList,
                onTransactionClick = { transaction ->
                    currentTransaction = transaction
                    dialogType = transaction.type
                    dialogAmountText = transaction.amount.toString()
                    dialogNoteText = transaction.note
                    dialogDate = transaction.timestamp
                    showEditDialog = true
                }
            )
        }
    }

    // --- ADD DIALOG ---
    if (showAddDialog) {
        TransactionDialog(
            title = "Add Entry",
            businessCategory = businessCategory,
            type = dialogType,
            amount = dialogAmountText,
            note = dialogNoteText,
            date = dialogDate,
            onAmountChange = { dialogAmountText = it },
            onNoteChange = { dialogNoteText = it },
            onDateChange = { dialogDate = it },
            onDismiss = { showAddDialog = false },
            onSave = {
                val amount = dialogAmountText.toDoubleOrNull()?.roundToInt() // Handle decimals safely
                if (amount != null && amount > 0) {
                    scope.launch {
                        val change = if (dialogType == "GOT") amount else -amount
                        val newBalance = customer.balance + change
                        val newTrans = Transaction(
                            customerId = customer.id,
                            amount = amount,
                            type = dialogType,
                            timestamp = dialogDate,
                            runningBalance = newBalance,
                            note = dialogNoteText
                        )
                        customerDao.insertTransaction(newTrans)
                        customerDao.updateCustomerBalance(customer.id, newBalance)
                        showAddDialog = false
                    }
                }
            },
            context = context
        )
    }

    // --- EDIT DIALOG ---
    if (showEditDialog && currentTransaction != null) {
        TransactionDialog(
            title = "Edit Entry",
            businessCategory = "General", // General mode for editing to keep it simple
            type = dialogType,
            amount = dialogAmountText,
            note = dialogNoteText,
            date = dialogDate,
            onAmountChange = { dialogAmountText = it },
            onNoteChange = { dialogNoteText = it },
            onDateChange = { dialogDate = it },
            onDismiss = { showEditDialog = false },
            onDelete = {
                scope.launch {
                    val oldAmount = currentTransaction!!.amount
                    val reverseChange = if (currentTransaction!!.type == "GOT") -oldAmount else oldAmount
                    val newBalance = customer.balance + reverseChange

                    customerDao.deleteTransaction(currentTransaction!!)
                    customerDao.updateCustomerBalance(customer.id, newBalance)
                    showEditDialog = false
                }
            },
            onSave = {
                val newAmount = dialogAmountText.toDoubleOrNull()?.roundToInt()
                if (newAmount != null && newAmount > 0) {
                    scope.launch {
                        val oldAmount = currentTransaction!!.amount
                        val revertOld = if (currentTransaction!!.type == "GOT") -oldAmount else oldAmount
                        val applyNew = if (dialogType == "GOT") newAmount else -newAmount
                        val finalBalance = customer.balance + revertOld + applyNew

                        val updatedTrans = currentTransaction!!.copy(
                            amount = newAmount,
                            type = dialogType,
                            note = dialogNoteText,
                            timestamp = dialogDate
                        )
                        customerDao.updateTransaction(updatedTrans)
                        customerDao.updateCustomerBalance(customer.id, finalBalance)
                        showEditDialog = false
                    }
                }
            },
            context = context
        )
    }
}

@Composable
fun TransactionDialog(
    title: String,
    businessCategory: String,
    type: String,
    amount: String,
    note: String,
    date: Long,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)? = null,
    context: Context
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(date))

    // DAIRY VARIABLES
    var weight by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }

    // --- UPDATED CALCULATOR LOGIC ---
    fun calculateDairy() {
        val w = weight.toDoubleOrNull() ?: 0.0
        val f = fat.toDoubleOrNull() ?: 0.0
        val r = rate.toDoubleOrNull() ?: 0.0

        if (w > 0 && f > 0 && r > 0) {
            // Formula: (Weight * Fat * Rate) / 100
            val total = ((w * f * r) / 100).roundToInt()
            onAmountChange(total.toString())

            // Save details to note
            onNoteChange("$w kg | $f Fat | Rate $r")
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            onDateChange(calendar.timeInMillis)
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = UdharGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateString, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Change", color = UdharGreenPrimary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (businessCategory == "Dairy") {
                    Text("Milk Calculator", fontSize = 12.sp, color = UdharGreenPrimary, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth()) {
                        // Weight (Decimal allowed)
                        OutlinedTextField(
                            value = weight, onValueChange = { weight = it; calculateDairy() },
                            label = { Text("Weight") }, modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Fat (Decimal allowed)
                        OutlinedTextField(
                            value = fat, onValueChange = { fat = it; calculateDairy() },
                            label = { Text("Fat") }, modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Rate (Decimal allowed)
                    OutlinedTextField(
                        value = rate, onValueChange = { rate = it; calculateDairy() },
                        label = { Text("Rate") }, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Final Amount (EDITABLE NOW)
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange, // User can now override this manually!
                    label = { Text("Total Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = false // <--- UNLOCKED: allows manual entry
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("Note / Details") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UdharRed)
                    }
                }
                TextButton(onClick = onSave) {
                    Text("SAVE", color = UdharGreenPrimary, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
        }
    )
}

@Composable
fun ActionButton(icon: ImageVector, text: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        color = Color.White,
        modifier = Modifier.width(100.dp).height(70.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        }
    }
}

// (Keep TransactionHistoryList and TransactionItem below as they were)
@Composable
fun TransactionHistoryList(
    transactionList: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    val groupedTransactions = transactionList.groupBy { formatDate(it.timestamp) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        groupedTransactions.forEach { (date, transactions) ->
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp) {
                        Text(date, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            items(transactions) { transaction ->
                TransactionItem(transaction, ::formatTime, onClick = { onTransactionClick(transaction) })
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    timeFormatter: (Long) -> String,
    onClick: () -> Unit
) {
    val isGot = transaction.type == "GOT"
    val amountColor = if (isGot) UdharGreenPrimary else UdharRed
    val bgColor = if (isGot) UdharGreenPrimary.copy(alpha = 0.1f) else UdharRed.copy(alpha = 0.1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isGot) {
            Box(modifier = Modifier.background(bgColor, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("₹ ${transaction.amount}", color = amountColor, fontWeight = FontWeight.Bold)
            }
        } else {
            Spacer(modifier = Modifier.width(60.dp))
        }

        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(timeFormatter(transaction.timestamp), fontSize = 12.sp, color = Color.Gray)
            if (transaction.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(transaction.note, fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Medium)
            }
        }

        if (isGot) {
            Box(modifier = Modifier.background(bgColor, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("₹ ${transaction.amount}", color = amountColor, fontWeight = FontWeight.Bold)
            }
        } else {
            Spacer(modifier = Modifier.width(60.dp))
        }
    }
}
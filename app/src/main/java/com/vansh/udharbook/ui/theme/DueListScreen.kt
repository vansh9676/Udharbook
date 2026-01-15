package com.vansh.udharbook.ui.theme

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vansh.udharbook.data.Customer
import com.vansh.udharbook.data.CustomerDao
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueListScreen(
    navController: NavController,
    customerDao: CustomerDao,
    businessId: Int
) {
    val context = LocalContext.current

    // Get customers for this business
    val allCustomers by customerDao.getCustomersForBusiness(businessId).collectAsState(initial = emptyList())

    // Filter: Only show people who OWE money (Balance is Negative)
    val dueList = allCustomers.filter { it.balance < 0 }
    val totalDue = dueList.sumOf { it.balance.absoluteValue }

    fun sendWhatsAppReminder(customer: Customer) {
        try {
            val message = "Hello ${customer.name}, your payment of ₹ ${customer.balance.absoluteValue} is pending. Please pay soon."
            val cleanNumber = customer.mobile.replace(" ", "").replace("+", "")
            val finalNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$finalNumber&text=${Uri.encode(message)}")
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not found", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Reminders", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UdharGreenPrimary)
            )
        },
        containerColor = UdharBackground
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Collection Pending", color = Color.Gray, fontSize = 14.sp)
                        Text("₹ $totalDue", color = UdharRed, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                    Surface(color = UdharRed.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text("${dueList.size} Customers", color = UdharRed, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (dueList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending payments! 🎉", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(dueList) { customer ->
                        DueCustomerItem(customer) { sendWhatsAppReminder(customer) }
                    }
                }
            }
        }
    }
}

@Composable
fun DueCustomerItem(customer: Customer, onRemindClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Due: ₹ ${customer.balance.absoluteValue}", color = UdharRed, fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = onRemindClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Remind", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
    Divider(color = Color.LightGray, thickness = 0.5.dp)
}
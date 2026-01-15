package com.vansh.udharbook.ui.theme

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.vansh.udharbook.data.Business
import com.vansh.udharbook.data.Customer
import com.vansh.udharbook.data.CustomerDao
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    customerDao: CustomerDao
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Get Phone Storage (SharedPreferences) to remember the choice forever
    val prefs = remember { context.getSharedPreferences("udharbook_prefs", Context.MODE_PRIVATE) }

    // 2. Fetch all businesses
    val businessList by customerDao.getAllBusinesses().collectAsState(initial = emptyList())

    // 3. State for selected business (Load from storage if available)
    var selectedBusinessId by remember {
        mutableStateOf<Int?>(
            if (prefs.contains("last_business_id")) {
                prefs.getInt("last_business_id", -1)
            } else {
                null
            }
        )
    }

    var showBusinessDialog by remember { mutableStateOf(false) }
    var newBusinessName by remember { mutableStateOf("") }
    var showAddBusinessDialog by remember { mutableStateOf(false) }

    // 4. Fallback: If no saved ID (or first run), pick the first business
    LaunchedEffect(businessList) {
        if (businessList.isNotEmpty()) {
            // If we have no selection yet, or the saved selection is invalid (e.g. deleted), pick the first one
            val isValidSelection = businessList.any { it.id == selectedBusinessId }
            if (selectedBusinessId == null || !isValidSelection) {
                selectedBusinessId = businessList.first().id
            }
        }
    }

    // 5. Watch for changes and SAVE them instantly
    LaunchedEffect(selectedBusinessId) {
        if (selectedBusinessId != null) {
            prefs.edit().putInt("last_business_id", selectedBusinessId!!).apply()
        }
    }

    // 6. Get customers for SELECTED business only
    val customersState = if (selectedBusinessId != null) {
        customerDao.getCustomersForBusiness(selectedBusinessId!!).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }
    val fullCustomerList = customersState.value

    // Calculations
    val totalYouWillGet = fullCustomerList.filter { it.balance > 0 }.sumOf { it.balance }
    val totalYouWillGive = fullCustomerList.filter { it.balance < 0 }.sumOf { it.balance.absoluteValue }

    var searchQuery by remember { mutableStateOf("") }
    val displayedList = if (searchQuery.isEmpty()) fullCustomerList else fullCustomerList.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.mobile.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .clickable { showBusinessDialog = true }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val currentName = businessList.find { it.id == selectedBusinessId }?.name ?: "UdharBook"
                            Text(currentName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Switch Profile ▼", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                },
                actions = {
                    // 1. NOTIFICATION / REMINDER ICON
                    IconButton(
                        onClick = {
                            if (selectedBusinessId != null) {
                                navController.navigate("due_list/$selectedBusinessId")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Reminders", tint = Color.White)
                    }

                    // 2. SETTINGS ICON
                    IconButton(
                        onClick = {
                            if (selectedBusinessId != null) {
                                navController.navigate("settings/$selectedBusinessId")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UdharGreenPrimary)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedBusinessId != null) {
                        navController.navigate("add_party/$selectedBusinessId")
                    }
                },
                containerColor = UdharRed,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("ADD CUSTOMER", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(UdharBackground)) {

            // Dashboard Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("You will give", color = Color.Gray)
                        Text("₹ $totalYouWillGive", color = UdharGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.LightGray))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("You will get", color = Color.Gray)
                        Text("₹ $totalYouWillGet", color = UdharRed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Divider() // Keep this divider

                // DELETE the old "View Reports" Row and PASTE this:
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. CASHBOOK BUTTON
                    TextButton(
                        onClick = {
                            if (selectedBusinessId != null) {
                                navController.navigate("cashbook/$selectedBusinessId")
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("CASHBOOK", color = UdharGreenPrimary, fontWeight = FontWeight.Bold)
                    }

                    // Vertical Divider
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))

                    // 2. REPORTS BUTTON
                    TextButton(
                        onClick = {
                            // TODO: Add Reports logic later
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("REPORTS", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Customer") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // List
            if (displayedList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                    Text("No customers in this profile.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(displayedList) { customer ->
                        CustomerItem(customer = customer) { clickedCustomerId ->
                            navController.navigate("customer_details/$clickedCustomerId")
                        }
                    }
                }
            }
        }
    }

    // --- SWITCH PROFILE DIALOG ---
    if (showBusinessDialog) {
        AlertDialog(
            onDismissRequest = { showBusinessDialog = false },
            title = { Text("Select Business Profile") },
            text = {
                Column {
                    businessList.forEach { business ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBusinessId = business.id // This triggers the save automatically
                                    showBusinessDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (business.id == selectedBusinessId),
                                onClick = { selectedBusinessId = business.id; showBusinessDialog = false },
                                colors = RadioButtonDefaults.colors(selectedColor = UdharGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(business.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Divider()
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddBusinessDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = UdharGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Add New Business")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBusinessDialog = false }) { Text("Close") }
            }
        )
    }

    // --- ADD NEW BUSINESS DIALOG (With Dairy Detection) ---
    if (showAddBusinessDialog) {
        AlertDialog(
            onDismissRequest = { showAddBusinessDialog = false },
            title = { Text("Create New Business") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newBusinessName,
                        onValueChange = { newBusinessName = it },
                        label = { Text("Business Name") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tip: Use 'Milk' or 'Dairy' for auto-calculator mode.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newBusinessName.isNotEmpty()) {
                            scope.launch {
                                // AUTO-DETECT CATEGORY
                                val nameLower = newBusinessName.lowercase()
                                val category = if (nameLower.contains("milk") || nameLower.contains("dairy")) {
                                    "Dairy"
                                } else {
                                    "General"
                                }

                                customerDao.insertBusiness(Business(name = newBusinessName, category = category))
                                newBusinessName = ""
                                showAddBusinessDialog = false
                                showBusinessDialog = false
                            }
                        }
                    }
                ) {
                    Text("CREATE", color = UdharGreenPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBusinessDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CustomerItem(customer: Customer, onClick: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).clickable { onClick(customer.id) }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = Color(0xFFE0E0E0), modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                val initials = if (customer.name.length >= 2) customer.name.take(2) else customer.name
                Text(initials.uppercase(), fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(customer.mobile, fontSize = 12.sp, color = Color.Gray)
        }
        val balanceText = "₹ ${customer.balance.absoluteValue}"
        val balanceColor = if (customer.balance >= 0) UdharGreenPrimary else UdharRed
        Text(balanceText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = balanceColor)
    }
    Divider(color = Color.LightGray, thickness = 0.5.dp)
}
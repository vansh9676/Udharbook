package com.vansh.udharbook.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vansh.udharbook.data.Customer
import com.vansh.udharbook.data.CustomerDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    navController: NavController,
    customerDao: CustomerDao,
    customerId: Int
) {
    val customerState by customerDao.getCustomer(customerId).collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    // Dialog States
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) } // <--- NEW STATE

    var editFieldLabel by remember { mutableStateOf("") }
    var editFieldValue by remember { mutableStateOf("") }
    var editingFieldType by remember { mutableStateOf("") }

    val customer = customerState ?: return

    fun openEdit(label: String, value: String, type: String) {
        editFieldLabel = label
        editFieldValue = value
        editingFieldType = type
        showEditDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UdharGreenPrimary)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- PHOTO ---
            Spacer(modifier = Modifier.height(24.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(shape = CircleShape, color = Color(0xFFF5F5F5), modifier = Modifier.size(100.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.padding(20.dp))
                }
                Surface(shape = CircleShape, color = UdharGreenPrimary, modifier = Modifier.size(32.dp).offset(x = 4.dp, y = 4.dp), border = BorderStroke(2.dp, Color.White)) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Edit", tint = Color.White, modifier = Modifier.padding(6.dp))
                }
            }
            Text("Add photo", color = UdharGreenPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // --- DETAILS ---
            ProfileItem(Icons.Default.Person, "Name", customer.name) { openEdit("Enter Name", customer.name, "NAME") }
            ProfileItem(Icons.Default.Call, "Mobile Number", customer.mobile) { openEdit("Enter Mobile", customer.mobile, "MOBILE") }
            ProfileItem(Icons.Default.LocationOn, "Address", customer.address.ifEmpty { "Add Address" }) { openEdit("Enter Address", customer.address, "ADDRESS") }

            Divider(color = Color(0xFFF0F0F0), thickness = 8.dp)
            ProfileItem(Icons.Default.SwapHoriz, "Role", customer.role) { }
            Divider(color = Color(0xFFF0F0F0), thickness = 8.dp)

            // --- DELETE BUTTON ---
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = { showDeleteConfirmation = true }, // <--- NOW OPENS DIALOG
                border = BorderStroke(1.dp, UdharRed),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = UdharRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DELETE CUSTOMER", color = UdharRed, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- DELETE CONFIRMATION DIALOG ---
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Customer?") },
            text = { Text("Are you sure you want to delete ${customer.name}? All transaction history for this customer will be permanently lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            customerDao.deleteCustomer(customerId)
                            showDeleteConfirmation = false
                            // Go back twice to reach Home Screen
                            navController.popBackStack()
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("DELETE", color = UdharRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }

    // --- EDIT DETAILS DIALOG ---
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Update Details") },
            text = {
                OutlinedTextField(
                    value = editFieldValue,
                    onValueChange = { editFieldValue = it },
                    label = { Text(editFieldLabel) },
                    singleLine = true,
                    keyboardOptions = if (editingFieldType == "MOBILE") KeyboardOptions(keyboardType = KeyboardType.Phone) else KeyboardOptions.Default
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val updatedCustomer = when (editingFieldType) {
                                "NAME" -> customer.copy(name = editFieldValue)
                                "MOBILE" -> customer.copy(mobile = editFieldValue)
                                "ADDRESS" -> customer.copy(address = editFieldValue)
                                else -> customer
                            }
                            customerDao.updateCustomer(updatedCustomer)
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("SAVE", color = UdharGreenPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("CANCEL", color = Color.Gray) }
            }
        )
    }
}

@Composable
fun ProfileItem(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (value == "Add Address") UdharGreenPrimary else Color.Black)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
    Divider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
}
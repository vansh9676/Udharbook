package com.vansh.udharbook.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vansh.udharbook.data.Customer
import com.vansh.udharbook.data.CustomerDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddPartyScreen(
    navController: NavController,
    customerDao: CustomerDao,
    businessId: Int // <--- NEW PARAMETER
) {
    var nameText by remember { mutableStateOf("") }
    var mobileText by remember { mutableStateOf("") }
    var isSupplierSelected by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Party", color = Color.White, fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    })
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .padding(bottom = 100.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Party name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UdharGreenPrimary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.height(56.dp).width(80.dp).border(1.dp, Color.Gray, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                        Text("+91", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = mobileText,
                        onValueChange = { mobileText = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UdharGreenPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Who are they?", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(Modifier.clickable { isSupplierSelected = false }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !isSupplierSelected, onClick = { isSupplierSelected = false }, colors = RadioButtonDefaults.colors(selectedColor = UdharGreenPrimary))
                        Text("Customer")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(Modifier.clickable { isSupplierSelected = true }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isSupplierSelected, onClick = { isSupplierSelected = true }, colors = RadioButtonDefaults.colors(selectedColor = UdharGreenPrimary))
                        Text("Supplier")
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.White).padding(16.dp).imePadding()) {
                Button(
                    onClick = {
                        if (nameText.isNotEmpty()) {
                            scope.launch {
                                val newCustomer = Customer(
                                    businessId = businessId, // <--- SAVING WITH BUSINESS ID
                                    name = nameText,
                                    mobile = mobileText,
                                    role = if(isSupplierSelected) "Supplier" else "Customer"
                                )
                                customerDao.insert(newCustomer)
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UdharGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ADD CUSTOMER", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
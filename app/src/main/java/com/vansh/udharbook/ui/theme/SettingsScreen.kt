package com.vansh.udharbook.ui.theme

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vansh.udharbook.data.CustomerDao
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    customerDao: CustomerDao,
    businessId: Int
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("udharbook_prefs", Context.MODE_PRIVATE) }

    // Load Business Data
    val businessList by customerDao.getAllBusinesses().collectAsState(initial = emptyList())
    val currentBusiness = businessList.find { it.id == businessId }

    var businessName by remember(currentBusiness) { mutableStateOf(currentBusiness?.name ?: "") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // App Lock States
    var isAppLockEnabled by remember { mutableStateOf(prefs.contains("app_pin")) }
    var showPinDialog by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }

    // --- BACKUP & RESTORE LAUNCHERS ---
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.sqlite3")) { uri ->
        if (uri != null) BackupRestoreHelper.backupData(context, uri)
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) BackupRestoreHelper.restoreData(context, uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Allow scrolling
        ) {

            // --- SECTION 1: BUSINESS ---
            Text("Business Profile", fontWeight = FontWeight.Bold, color = UdharGreenPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (currentBusiness != null && businessName.isNotEmpty()) {
                        scope.launch {
                            customerDao.updateBusiness(currentBusiness.copy(name = businessName))
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UdharGreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE NAME", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(32.dp))

            // --- SECTION 2: SECURITY ---
            Text("Security", fontWeight = FontWeight.Bold, color = UdharGreenPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("App Lock", fontWeight = FontWeight.Bold)
                        Text(if (isAppLockEnabled) "PIN Active" else "No PIN set", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Switch(
                    checked = isAppLockEnabled,
                    onCheckedChange = { shouldEnable ->
                        if (shouldEnable) {
                            newPin = ""
                            showPinDialog = true
                        } else {
                            prefs.edit().remove("app_pin").apply()
                            isAppLockEnabled = false
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = UdharGreenPrimary)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(32.dp))

            // --- SECTION 3: DATA MANAGEMENT (BACKUP) ---
            Text("Data Backup & Restore", fontWeight = FontWeight.Bold, color = UdharGreenPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            // BACKUP BUTTON
            OutlinedButton(
                onClick = {
                    val date = SimpleDateFormat("ddMMM_HHmm", Locale.getDefault()).format(Date())
                    val fileName = "UdharBook_Backup_$date.db"
                    backupLauncher.launch(fileName)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = UdharGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CREATE BACKUP", color = UdharGreenPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RESTORE BUTTON
            OutlinedButton(
                onClick = {
                    restoreLauncher.launch(arrayOf("*/*")) // Allow all file selection, user must pick DB
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, tint = Color.DarkGray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESTORE DATA", color = Color.DarkGray, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(32.dp))

            // --- SECTION 4: DANGER ZONE ---
            if (businessList.size > 1) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    border = BorderStroke(1.dp, UdharRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = UdharRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DELETE BUSINESS PROFILE", color = UdharRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // (Keep the Dialogs for PIN and Delete here - same as before)
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set New App PIN") },
            text = {
                Column {
                    Text("Enter a 4-digit PIN to secure your app.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4) newPin = it },
                        label = { Text("4-Digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            prefs.edit().putString("app_pin", newPin).apply()
                            isAppLockEnabled = true
                            showPinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UdharGreenPrimary)
                ) {
                    Text("SET PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (showDeleteDialog && currentBusiness != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Profile?") },
            text = { Text("Are you sure you want to delete '${currentBusiness.name}'? All data will be lost forever.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            customerDao.deleteBusiness(currentBusiness)
                            showDeleteDialog = false
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("DELETE", color = UdharRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL", color = Color.Gray) }
            }
        )
    }
}
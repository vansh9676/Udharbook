package com.vansh.udharbook.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape // <--- THIS WAS MISSING
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AppLockScreen(
    navController: NavController,
    storedPin: String
) {
    var currentPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Check PIN every time it changes
    LaunchedEffect(currentPin) {
        if (currentPin.length == 4) {
            if (currentPin == storedPin) {
                // Correct PIN! Go to Home
                navController.navigate("home") {
                    popUpTo("app_lock") { inclusive = true }
                }
            } else {
                // Wrong PIN
                errorMessage = "Wrong PIN. Try again."
                currentPin = ""
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(UdharGreenPrimary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("UdharBook Locked", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter your 4-digit PIN", color = Color.White.copy(alpha = 0.8f))

        Spacer(modifier = Modifier.height(32.dp))

        // DOTS DISPLAY
        Row(horizontalArrangement = Arrangement.Center) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(16.dp)
                        .background(
                            if (index < currentPin.length) Color.White else Color.White.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                errorMessage,
                color = UdharRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // NUMBER PAD
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "<")
            )

            for (row in rows) {
                Row(modifier = Modifier.padding(8.dp)) {
                    for (key in row) {
                        PinButton(key) {
                            if (key == "<") {
                                if (currentPin.isNotEmpty()) currentPin = currentPin.dropLast(1)
                            } else if (key.isNotEmpty()) {
                                if (currentPin.length < 4) currentPin += key
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(12.dp)
            .size(70.dp)
            .background(if (text.isEmpty()) Color.Transparent else Color.White.copy(alpha = 0.2f), CircleShape)
            .border(1.dp, if (text.isEmpty()) Color.Transparent else Color.White.copy(alpha = 0.5f), CircleShape)
            .clickable(enabled = text.isNotEmpty()) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text == "<") {
            Text("DEL", color = Color.White, fontWeight = FontWeight.Bold)
        } else {
            Text(text, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
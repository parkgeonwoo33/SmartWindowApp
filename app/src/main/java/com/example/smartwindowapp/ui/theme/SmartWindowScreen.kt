package com.example.smartwindowapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartwindowapp.viewmodel.SmartWindowViewModel

@Composable
fun SmartWindowScreen(viewModel: SmartWindowViewModel = viewModel()) {
    val status by viewModel.sensorStatus.collectAsState()
    var isManualMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Air Quality", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Window: ${if (isManualMode) "Manual" else "Auto"}", fontSize = 18.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoCard("CO₂", "${status?.co2 ?: "-"} ppm")
            InfoCard(
                "Temperature",
                status?.temperature?.let { "%.1f".format(it) + " ℃" } ?: "-"
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoCard(
                "Humidity",
                status?.humidity?.let { "%.1f".format(it) + " %" } ?: "-"
            )
            InfoCard("PM₂.₅", "${status?.pm25 ?: "-"} µg/m³")
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE0F0EF))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModeButton("Auto", !isManualMode) { isManualMode = false }
            ModeButton("Manual", isManualMode) { isManualMode = true }
        }

        if (isManualMode) {
            Button(
                onClick = { viewModel.openWindow() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("OPEN")
            }
            Button(
                onClick = { viewModel.closeWindow() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CLOSE")
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .size(width = 150.dp, height = 100.dp)
            .border(1.dp, Color(0xFF004F4F), shape = RoundedCornerShape(12.dp))
            .background(Color(0xFFE0F0EF))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModeButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (selected) Color(0xFF006666) else Color(0xFFE0F0EF))
            .clickable { onClick() }
            .padding(horizontal = 32.dp, vertical = 12.dp)
    ) {
        Text(text, color = if (selected) Color.White else Color.Black)
    }
}

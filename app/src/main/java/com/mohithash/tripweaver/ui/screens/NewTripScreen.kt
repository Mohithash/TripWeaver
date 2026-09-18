@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.tripweaver.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.tripweaver.domain.TripBrief
import com.mohithash.tripweaver.ui.AppViewModel
import com.mohithash.tripweaver.ui.Label
import com.mohithash.tripweaver.ui.StatCard
import java.time.LocalDate

@Composable
fun NewTripScreen(vm: AppViewModel, onBack: () -> Unit, onCreated: () -> Unit) {
    val ai by vm.ai.collectAsState()
    val cs = MaterialTheme.colorScheme
    var dest by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(LocalDate.now().plusWeeks(2).toString()) }
    var days by remember { mutableStateOf(4f) }
    var who by remember { mutableStateOf("couple") }
    var budget by remember { mutableStateOf("mid‑range") }
    var pace by remember { mutableStateOf("balanced") }
    var interests by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("Plan a trip") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard {
                OutlinedTextField(dest, { dest = it }, label = { Text("Destination") }, placeholder = { Text("Lisbon, Kyoto, Bali…") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
                OutlinedTextField(start, { start = it }, label = { Text("Start date (yyyy-mm-dd)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
                Label("Days: ${days.toInt()}"); Slider(days, { days = it }, valueRange = 1f..14f, steps = 12)
                Label("Who"); FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("solo", "couple", "family", "friends").forEach { w -> FilterChip(selected = who == w, onClick = { who = w }, label = { Text(w) }) } }
                Label("Budget"); FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("shoestring", "mid‑range", "splurge").forEach { w -> FilterChip(selected = budget == w, onClick = { budget = w }, label = { Text(w) }) } }
                Label("Pace"); FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("relaxed", "balanced", "packed").forEach { w -> FilterChip(selected = pace == w, onClick = { pace = w }, label = { Text(w) }) } }
                OutlinedTextField(interests, { interests = it }, label = { Text("Interests") }, placeholder = { Text("food, museums, hiking, nightlife, kids…") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
            }
            if (!ai.configured) Text("Add an API key in Settings to generate itineraries.", color = cs.error, style = MaterialTheme.typography.bodySmall)
            Button({ vm.create(TripBrief(dest.trim(), start.trim(), days.toInt(), who, budget, pace, interests.trim()), onCreated) }, enabled = dest.isNotBlank() && ai.configured, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Weave my itinerary", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

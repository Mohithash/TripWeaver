@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.tripweaver.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.tripweaver.ui.AppViewModel
import com.mohithash.tripweaver.ui.EmptyState
import com.mohithash.tripweaver.ui.HeroCard
import com.mohithash.tripweaver.ui.Label
import com.mohithash.tripweaver.ui.ShapeIcon
import com.mohithash.tripweaver.ui.StatCard
import com.mohithash.tripweaver.ui.prettyDate
import com.mohithash.tripweaver.ui.theme.Brand
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun TripsScreen(vm: AppViewModel, onNew: () -> Unit, onOpen: () -> Unit, onSettings: () -> Unit) {
    val trips by vm.trips.collectAsState()
    val cs = MaterialTheme.colorScheme
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val next = trips.filter { runCatching { LocalDate.parse(it.startDate) >= LocalDate.now() }.getOrDefault(false) }.minByOrNull { it.startDate }
    Scaffold(modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { MediumFlexibleTopAppBar(title = { Text("Trips") }, subtitle = { Text("${trips.size} planned") }, actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }, scrollBehavior = scroll, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface, scrolledContainerColor = cs.surface)) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onNew, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Plan a trip") }, containerColor = cs.primary, contentColor = cs.onPrimary) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Cookie12Sided) {
                    val on = cs.onPrimary
                    if (next != null) {
                        val d = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(next.startDate))
                        Label("Next up", on.copy(alpha = 0.8f)); Text(next.destination, style = MaterialTheme.typography.displaySmall, color = on)
                        Text(if (d == 0L) "Today! ${next.days} days" else "in $d days · ${next.days} days", color = on.copy(alpha = 0.85f))
                    } else { Label("Where to?", on.copy(alpha = 0.8f)); Text("Plan your next trip", style = MaterialTheme.typography.headlineMedium, color = on); Text("Day‑by‑day itinerary, packing list, tips and phrases — in a minute.", color = on.copy(alpha = 0.85f)) }
                }
            }
            if (trips.isEmpty()) item { EmptyState(Icons.Default.Luggage, "No trips yet", "Tap Plan a trip to get started.") }
            items(trips, key = { it.id }) { t ->
                StatCard(Modifier.clip(MaterialTheme.shapes.extraLarge).clickable { vm.open(t); onOpen() }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ShapeIcon(Icons.Default.Flight, cs.secondaryContainer, cs.onSecondaryContainer, MaterialShapes.Sunny)
                        Column(Modifier.weight(1f)) { Text(t.destination, style = MaterialTheme.typography.titleMedium); Text("${t.startDate.prettyDate()} · ${t.days} days" + if (t.itinerary.isBlank()) " · not planned yet" else "", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant) }
                        IconButton({ vm.delete(t.id) }) { Icon(Icons.Default.Delete, null, tint = cs.onSurfaceVariant) }
                    }
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

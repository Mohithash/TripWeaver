@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.tripweaver.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mohithash.tripweaver.ui.AppViewModel
import com.mohithash.tripweaver.ui.HeroCard
import com.mohithash.tripweaver.ui.Job
import com.mohithash.tripweaver.ui.Label
import com.mohithash.tripweaver.ui.StatCard
import com.mohithash.tripweaver.ui.prettyDate
import com.mohithash.tripweaver.ui.theme.Brand
import java.time.LocalDate

private enum class Tab { PLAN, PACK, TIPS, ASK }

@Composable
fun TripScreen(vm: AppViewModel, onBack: () -> Unit) {
    val sel by vm.selected.collectAsState()
    val trip = sel ?: run { onBack(); return }
    val it = vm.itinerary(trip)
    val job by vm.plan.collectAsState()
    val redoing by vm.redoing.collectAsState()
    val cs = MaterialTheme.colorScheme
    var tab by remember { mutableStateOf(Tab.PLAN) }
    var redoIdx by remember { mutableStateOf(-1) }
    var wish by remember { mutableStateOf("") }

    Scaffold(topBar = {
        TopAppBar(title = { Column { Text(trip.destination); Text("${trip.startDate.prettyDate()} · ${trip.days} days", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            actions = { if (it != null) IconButton({ vm.generate() }, enabled = job != Job.Loading) { Icon(Icons.Default.Refresh, "Regenerate") } })
    }) { pad ->
        if (it == null) {
            Column(Modifier.fillMaxSize().padding(pad).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                when (val j = job) {
                    is Job.Failed -> { Text(j.message, color = cs.error); Spacer(Modifier.height(12.dp)); Button({ vm.generate() }, shapes = ButtonDefaults.shapes()) { Text("Retry") } }
                    else -> { LoadingIndicator(Modifier.size(64.dp)); Spacer(Modifier.height(16.dp)); Text("Weaving ${trip.days} days in ${trip.destination}…", style = MaterialTheme.typography.titleMedium); Text("Itinerary, packing list, tips and phrases.", color = cs.onSurfaceVariant) }
                }
            }
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                Tab.entries.forEachIndexed { i, t ->
                    ToggleButton(checked = tab == t, onCheckedChange = { tab = t }, modifier = Modifier.weight(1f), shapes = when (i) { 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes(); Tab.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes(); else -> ButtonGroupDefaults.connectedMiddleButtonShapes() }) {
                        Text(t.name.lowercase().replaceFirstChar { c -> c.uppercase() }, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            if (job == Job.Loading) LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            AnimatedContent(tab, label = "tab") { t ->
                when (t) {
                    Tab.PLAN -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item { HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Cookie9Sided) { Text(it.title, style = MaterialTheme.typography.headlineSmall, color = cs.onPrimary); Text(it.overview, color = cs.onPrimary.copy(alpha = 0.9f)); if (it.budget_estimate.isNotBlank()) Text("💰 ${it.budget_estimate}", color = cs.onPrimary.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge) } }
                        itemsIndexed(it.days) { i, d ->
                            val date = runCatching { LocalDate.parse(trip.startDate).plusDays(i.toLong()).toString().prettyDate() }.getOrDefault("")
                            StatCard {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column { Label("Day ${d.day} · $date"); Text(d.title, style = MaterialTheme.typography.titleLarge) }
                                    if (redoing == i) LoadingIndicator(Modifier.size(24.dp)) else IconButton({ redoIdx = i; wish = "" }) { Icon(Icons.Default.Refresh, "Redo day", tint = cs.primary) }
                                }
                                d.activities.forEach { a ->
                                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(a.time, style = MaterialTheme.typography.labelLarge, color = cs.primary, modifier = Modifier.width(52.dp)); Box(Modifier.padding(top = 4.dp).size(8.dp).background(cs.primary, CircleShape)) }
                                        Column(Modifier.weight(1f)) {
                                            Text(a.title, style = MaterialTheme.typography.titleMedium)
                                            if (a.detail.isNotBlank()) Text(a.detail, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                                            Text(listOf(a.area, a.cost_hint).filter { s -> s.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = cs.tertiary)
                                        }
                                    }
                                }
                                if (d.food_tip.isNotBlank()) Text("🍽️ ${d.food_tip}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                    Tab.PACK -> {
                        val packed = vm.packed(trip)
                        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            item { LinearWavyProgressIndicator(progress = { if (it.packing.isEmpty()) 0f else packed.size.toFloat() / it.packing.size }, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)); Text("${packed.size} of ${it.packing.size} packed", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant) }
                            it.packing.withIndex().groupBy { p -> p.value.category }.forEach { (cat, list) ->
                                item { Text(cat, style = MaterialTheme.typography.titleMedium, color = cs.primary, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
                                items(list, key = { p -> p.index }) { p ->
                                    val done = p.index in packed
                                    Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(if (done) cs.surfaceContainerLowest else cs.surfaceContainerLow).clickable { vm.togglePacked(p.index) }.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(done, { vm.togglePacked(p.index) }); Text(p.value.item, textDecoration = if (done) TextDecoration.LineThrough else null, color = if (done) cs.onSurfaceVariant else cs.onSurface)
                                    }
                                }
                            }
                        }
                    }
                    Tab.TIPS -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item { StatCard(container = cs.secondaryContainer) { Label("Good to know", cs.onSecondaryContainer); it.tips.forEach { t -> Text("•  $t", color = cs.onSecondaryContainer, style = MaterialTheme.typography.bodyLarge) } } }
                        if (it.phrases.isNotEmpty()) item { StatCard(container = cs.tertiaryContainer) { Label("Phrases", cs.onTertiaryContainer); it.phrases.forEach { p -> Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(p.phrase, style = MaterialTheme.typography.titleMedium, color = cs.onTertiaryContainer); Text(p.meaning, color = cs.onTertiaryContainer.copy(alpha = 0.85f)) } } } }
                    }
                    Tab.ASK -> AskTab(vm)
                }
            }
        }
    }
    if (redoIdx >= 0) AlertDialog(onDismissRequest = { redoIdx = -1 }, title = { Text("Redo day ${redoIdx + 1}") },
        text = { OutlinedTextField(wish, { wish = it }, placeholder = { Text("e.g. more relaxed, rainy‑day plan, kid‑friendly, cheaper") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) },
        confirmButton = { TextButton({ vm.redoDay(redoIdx, wish.ifBlank { "a fresh alternative" }); redoIdx = -1 }) { Text("Redo") } }, dismissButton = { TextButton({ redoIdx = -1 }) { Text("Cancel") } })
}

@Composable
private fun AskTab(vm: AppViewModel) {
    val cs = MaterialTheme.colorScheme
    val ans by vm.ask.collectAsState()
    var q by remember { mutableStateOf("") }
    var asked by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ask about your trip — “is tap water safe?”, “how do I get from the airport?”, “what's the tipping custom?”", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(q, { q = it }, placeholder = { Text("Your question") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.extraLarge, maxLines = 3)
            FilledIconButton({ asked = q; vm.askAssistant(q); q = "" }, enabled = q.isNotBlank() && ans != Job.Loading, modifier = Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Filled.Send, null) }
        }
        if (asked.isNotBlank()) StatCard(container = cs.secondaryContainer) { Text(asked, color = cs.onSecondaryContainer, style = MaterialTheme.typography.titleMedium) }
        when (val a = ans) {
            Job.Loading -> Row(verticalAlignment = Alignment.CenterVertically) { LoadingIndicator(); Spacer(Modifier.size(10.dp)); Text("Thinking…", color = cs.onSurfaceVariant) }
            is Job.Done -> StatCard { Text(a.value, style = MaterialTheme.typography.bodyLarge) }
            is Job.Failed -> Text(a.message, color = cs.error)
            Job.Idle -> {}
        }
    }
}

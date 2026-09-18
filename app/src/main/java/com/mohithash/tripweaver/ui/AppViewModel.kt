package com.mohithash.tripweaver.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohithash.tripweaver.App
import com.mohithash.tripweaver.ai.AiSettings
import com.mohithash.tripweaver.data.Trip
import com.mohithash.tripweaver.domain.Itinerary
import com.mohithash.tripweaver.domain.Settings
import com.mohithash.tripweaver.domain.TripBrief
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed interface Job<out T> {
    data object Idle : Job<Nothing>
    data object Loading : Job<Nothing>
    data class Done<T>(val value: T) : Job<T>
    data class Failed(val message: String) : Job<Nothing>
}

class AppViewModel(private val app: App) : ViewModel() {
    private val db = app.db
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    val client get() = app.client

    val ai: StateFlow<AiSettings> = app.store.flow("ai", AiSettings.serializer(), AiSettings())
    val settings: StateFlow<Settings> = app.store.flow("settings", Settings.serializer(), Settings())
    fun saveAi(a: AiSettings) = app.store.set("ai", AiSettings.serializer(), a)
    fun saveSettings(s: Settings) = app.store.set("settings", Settings.serializer(), s.copy(onboarded = true))

    val trips = db.trips().all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val selected = MutableStateFlow<Trip?>(null)
    private val _plan = MutableStateFlow<Job<Unit>>(Job.Idle)
    val plan: StateFlow<Job<Unit>> = _plan
    val redoing = MutableStateFlow(-1)
    private val _ask = MutableStateFlow<Job<String>>(Job.Idle)
    val ask: StateFlow<Job<String>> = _ask

    fun brief(t: Trip): TripBrief = runCatching { json.decodeFromString(TripBrief.serializer(), t.brief) }.getOrDefault(TripBrief())
    fun itinerary(t: Trip): Itinerary? = t.itinerary.takeIf { it.isNotBlank() }?.let { runCatching { json.decodeFromString(Itinerary.serializer(), it) }.getOrNull() }
    fun packed(t: Trip): Set<Int> = t.packed.split(',').mapNotNull { it.toIntOrNull() }.toSet()

    fun create(b: TripBrief, onReady: () -> Unit) = viewModelScope.launch {
        val row = Trip(destination = b.destination, startDate = b.startDate, days = b.days, brief = json.encodeToString(TripBrief.serializer(), b))
        val id = db.trips().insert(row)
        selected.value = row.copy(id = id); _plan.value = Job.Idle; onReady(); generate()
    }
    fun open(t: Trip) { selected.value = t; _plan.value = Job.Idle; _ask.value = Job.Idle }
    fun delete(id: Long) = viewModelScope.launch { db.trips().delete(id); if (selected.value?.id == id) selected.value = null }

    fun generate() {
        val t = selected.value ?: return
        _plan.value = Job.Loading
        viewModelScope.launch {
            _plan.value = runCatching { app.trips.plan(ai.value, settings.value, brief(t)) }.fold({ it ->
                val u = t.copy(itinerary = json.encodeToString(Itinerary.serializer(), it), packed = ""); db.trips().update(u); selected.value = u; Job.Done(Unit)
            }, { Job.Failed(it.message ?: "Failed") })
        }
    }
    fun redoDay(idx: Int, wish: String) {
        val t = selected.value ?: return; val it = itinerary(t) ?: return
        redoing.value = idx
        viewModelScope.launch {
            runCatching { app.trips.redoDay(ai.value, settings.value, brief(t), it, idx, wish) }.onSuccess { d ->
                val u = t.copy(itinerary = json.encodeToString(Itinerary.serializer(), it.copy(days = it.days.toMutableList().also { l -> l[idx] = d })))
                db.trips().update(u); selected.value = u
            }.onFailure { e -> _plan.value = Job.Failed(e.message ?: "Failed") }
            redoing.value = -1
        }
    }
    fun togglePacked(idx: Int) = viewModelScope.launch {
        val t = selected.value ?: return@launch
        val p = packed(t).toMutableSet(); if (!p.add(idx)) p.remove(idx)
        val u = t.copy(packed = p.joinToString(",")); db.trips().update(u); selected.value = u
    }
    fun askAssistant(q: String) {
        val t = selected.value ?: return
        _ask.value = Job.Loading
        viewModelScope.launch { _ask.value = runCatching { app.trips.ask(ai.value, settings.value, brief(t), q) }.fold({ Job.Done(it) }, { Job.Failed(it.message ?: "Failed") }) }
    }
}

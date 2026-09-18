package com.mohithash.tripweaver.domain

import kotlinx.serialization.Serializable

@Serializable
data class TripBrief(
    val destination: String = "",
    val startDate: String = "",
    val days: Int = 4,
    val travellers: String = "couple",     // solo | couple | family | friends
    val budget: String = "mid‑range",      // shoestring | mid‑range | splurge
    val pace: String = "balanced",         // relaxed | balanced | packed
    val interests: String = "",
)

@Serializable data class Activity(val time: String, val title: String, val detail: String = "", val area: String = "", val cost_hint: String = "")
@Serializable data class DayPlan(val day: Int, val title: String, val activities: List<Activity> = emptyList(), val food_tip: String = "")
@Serializable data class PackItem(val item: String, val category: String = "Essentials")
@Serializable data class Phrase(val phrase: String, val meaning: String)
@Serializable
data class Itinerary(
    val title: String = "",
    val overview: String = "",
    val days: List<DayPlan> = emptyList(),
    val tips: List<String> = emptyList(),
    val packing: List<PackItem> = emptyList(),
    val phrases: List<Phrase> = emptyList(),
    val budget_estimate: String = "",
)

@Serializable data class Settings(val home: String = "", val onboarded: Boolean = false)

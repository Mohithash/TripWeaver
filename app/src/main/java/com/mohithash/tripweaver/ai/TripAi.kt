package com.mohithash.tripweaver.ai

import com.mohithash.tripweaver.domain.DayPlan
import com.mohithash.tripweaver.domain.Itinerary
import com.mohithash.tripweaver.domain.Settings
import com.mohithash.tripweaver.domain.TripBrief

class TripAi(private val client: AiClient) {
    private val activity = Schema.obj("time" to Schema.str, "title" to Schema.str, "detail" to Schema.str, "area" to Schema.str, "cost_hint" to Schema.str)
    private val day = Schema.obj("day" to Schema.int, "title" to Schema.str, "activities" to Schema.arr(activity), "food_tip" to Schema.str)
    private val itinerarySchema = Schema.obj(
        "title" to Schema.str, "overview" to Schema.str, "days" to Schema.arr(day), "tips" to Schema.arr(Schema.str),
        "packing" to Schema.arr(Schema.obj("item" to Schema.str, "category" to Schema.enum("Essentials", "Clothing", "Toiletries", "Tech", "Documents", "Other"))),
        "phrases" to Schema.arr(Schema.obj("phrase" to Schema.str, "meaning" to Schema.str)), "budget_estimate" to Schema.str,
    )

    private fun brief(b: TripBrief, s: Settings) = "Destination: ${b.destination}. Start ${b.startDate.ifBlank { "unspecified" }}, ${b.days} days. Travellers: ${b.travellers}. Budget: ${b.budget}. Pace: ${b.pace}." +
        (if (b.interests.isNotBlank()) " Interests: ${b.interests}." else "") + (if (s.home.isNotBlank()) " Home country: ${s.home}." else "")

    suspend fun plan(ai: AiSettings, s: Settings, b: TripBrief): Itinerary {
        val system = """You are a seasoned travel planner who knows real neighbourhoods, opening patterns and logistics. Plan exactly ${b.days} days.
            |Each day: a title, 3-5 activities in chronological order with realistic times, grouped geographically to avoid backtracking, a one-line detail, the area/neighbourhood and a cost hint. food_tip: one specific local dish or place type.
            |tips: 5-8 practical tips (transport, money, etiquette, safety, booking-ahead). packing: 12-20 items appropriate to the season and activities. phrases: 6 useful local-language phrases (empty list if the local language matches the traveller's). budget_estimate: one line, per person, excluding flights.
            |Do not invent exact prices or specific business names you are not confident exist; prefer landmarks, districts and types of places.""".trimMargin()
        return client.ask(ai, system, brief(b, s), itinerarySchema, maxTokens = 12000)
    }

    suspend fun redoDay(ai: AiSettings, s: Settings, b: TripBrief, it: Itinerary, dayIdx: Int, wish: String): DayPlan {
        val system = "You revise ONE day of an existing itinerary. Keep the same day number. Avoid repeating activities from other days. Follow the traveller's wish: \"$wish\". ${brief(b, s)}"
        val user = "Other days:\n" + it.days.filterIndexed { i, _ -> i != dayIdx }.joinToString("\n") { d -> "Day ${d.day}: ${d.activities.joinToString { a -> a.title }}" } + "\n\nRevise day ${it.days[dayIdx].day} (currently: ${it.days[dayIdx].activities.joinToString { a -> a.title }})."
        return client.ask(ai, system, user, day, maxTokens = 3000)
    }

    suspend fun ask(ai: AiSettings, s: Settings, b: TripBrief, question: String): String =
        client.chat(ai, "You are a knowledgeable, honest travel assistant. Answer in under 120 words. If unsure about current facts (prices, hours), say so and suggest how to verify. ${brief(b, s)}", listOf(ChatMsg("user", question)), maxTokens = 600)
}

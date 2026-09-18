@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.tripweaver.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohithash.tripweaver.ui.screens.NewTripScreen
import com.mohithash.tripweaver.ui.screens.OnboardingScreen
import com.mohithash.tripweaver.ui.screens.SettingsScreen
import com.mohithash.tripweaver.ui.screens.TripScreen
import com.mohithash.tripweaver.ui.screens.TripsScreen

@Composable
fun Nav(vm: AppViewModel) {
    val s by vm.settings.collectAsState()
    if (!s.onboarded) { OnboardingScreen(vm); return }
    val nav = rememberNavController()
    NavHost(nav, "trips") {
        composable("trips") { TripsScreen(vm, onNew = { nav.navigate("new") }, onOpen = { nav.navigate("trip") }, onSettings = { nav.navigate("settings") }) }
        composable("new") { NewTripScreen(vm, onBack = { nav.popBackStack() }, onCreated = { nav.popBackStack(); nav.navigate("trip") }) }
        composable("trip") { TripScreen(vm, onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
    }
}

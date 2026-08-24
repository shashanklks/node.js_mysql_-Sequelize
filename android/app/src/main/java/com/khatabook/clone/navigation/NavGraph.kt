package com.khatabook.clone.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.khatabook.clone.data.remote.EntryType
import com.khatabook.clone.data.remote.PartyType
import com.khatabook.clone.ui.auth.LanguageScreen
import com.khatabook.clone.ui.auth.LoginScreen
import com.khatabook.clone.ui.auth.OtpScreen
import com.khatabook.clone.ui.auth.ProfileSetupScreen
import com.khatabook.clone.ui.auth.SplashScreen
import com.khatabook.clone.ui.home.HomeScreen
import com.khatabook.clone.ui.party.AddEntryScreen
import com.khatabook.clone.ui.party.AddPartyScreen
import com.khatabook.clone.ui.party.PartyDetailScreen
import com.khatabook.clone.ui.profile.ProfileScreen
import com.khatabook.clone.ui.reports.ReportsScreen

object Routes {
    const val SPLASH = "splash"
    const val LANGUAGE = "language"
    const val LOGIN = "login"
    const val OTP = "otp/{phone}?devOtp={devOtp}"
    const val PROFILE_SETUP = "profile-setup"
    const val HOME = "home"
    const val ADD_PARTY = "add-party/{type}"
    const val PARTY = "party/{partyId}"
    const val ADD_ENTRY = "add-entry/{partyId}/{type}"
    const val REPORTS = "reports"
    const val PROFILE = "profile"

    fun otp(phone: String, devOtp: String?) = "otp/$phone?devOtp=${devOtp.orEmpty()}"
    fun addParty(type: String) = "add-party/$type"
    fun party(partyId: Int) = "party/$partyId"
    fun addEntry(partyId: Int, type: String) = "add-entry/$partyId/$type"
}

@Composable
fun KhatabookNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onLoggedOut = { languageChosen ->
                    val target = if (languageChosen) Routes.LOGIN else Routes.LANGUAGE
                    navController.navigate(target) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.LANGUAGE) {
            LanguageScreen(onContinue = { navController.navigate(Routes.LOGIN) })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onOtpSent = { phone, devOtp -> navController.navigate(Routes.otp(phone, devOtp)) }
            )
        }

        composable(
            route = Routes.OTP,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("devOtp") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            OtpScreen(
                phone = entry.arguments?.getString("phone").orEmpty(),
                devOtp = entry.arguments?.getString("devOtp")?.takeIf { it.isNotBlank() },
                onBack = { navController.popBackStack() },
                onVerified = { needsProfile ->
                    val target = if (needsProfile) Routes.PROFILE_SETUP else Routes.HOME
                    navController.navigate(target) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onOpenParty = { navController.navigate(Routes.party(it)) },
                onAddParty = { type -> navController.navigate(Routes.addParty(type)) },
                onQuickEntry = { id, type -> navController.navigate(Routes.addEntry(id, type)) },
                onOpenReports = { navController.navigate(Routes.REPORTS) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
            )
        }

        composable(
            route = Routes.ADD_PARTY,
            arguments = listOf(navArgument("type") { type = NavType.StringType }),
        ) { entry ->
            AddPartyScreen(
                initialType = entry.arguments?.getString("type") ?: PartyType.CUSTOMER,
                onBack = { navController.popBackStack() },
                onCreated = { partyId ->
                    // Straight into the new ledger, the way the app does it.
                    navController.navigate(Routes.party(partyId)) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }

        composable(
            route = Routes.PARTY,
            arguments = listOf(navArgument("partyId") { type = NavType.IntType }),
        ) { entry ->
            val partyId = entry.arguments?.getInt("partyId") ?: 0
            PartyDetailScreen(
                partyId = partyId,
                onBack = { navController.popBackStack() },
                onAddEntry = { id, type -> navController.navigate(Routes.addEntry(id, type)) },
            )
        }

        composable(
            route = Routes.ADD_ENTRY,
            arguments = listOf(
                navArgument("partyId") { type = NavType.IntType },
                navArgument("type") { type = NavType.StringType },
            ),
        ) { entry ->
            AddEntryScreen(
                partyId = entry.arguments?.getInt("partyId") ?: 0,
                type = entry.arguments?.getString("type") ?: EntryType.GAVE,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.REPORTS) {
            ReportsScreen(
                onBack = { navController.popBackStack() },
                onHome = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onProfile = { navController.navigate(Routes.PROFILE) },
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onHome = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onReports = { navController.navigate(Routes.REPORTS) },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}

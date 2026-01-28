package com.github.junsik.markdown.mermaid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.junsik.markdown.mermaid.core.ui.theme.PremiumTheme
import com.github.junsik.markdown.mermaid.feature.home.HomeRoute
import com.github.junsik.markdown.mermaid.feature.settings.SettingsRoute
import com.github.junsik.markdown.mermaid.feature.settings.SettingsViewModel
import com.github.junsik.markdown.mermaid.feature.viewer.ViewerRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentUri = intent?.data
        if (intentUri != null && intent?.flags?.and(Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                contentResolver.takePersistableUriPermission(
                    intentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
        }
        val intentUriString = intentUri?.toString()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isTableFormat by settingsViewModel.isTableFormat.collectAsStateWithLifecycle()

            PremiumTheme(darkTheme = isDarkTheme) {
                MarkdownMermaidApp(
                    intentUri = intentUriString,
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = settingsViewModel::onThemeChanged,
                    isTableFormat = isTableFormat,
                    onTableFormatChanged = settingsViewModel::onTableFormatChanged
                )
            }
        }
    }
}

fun encodeUri(uri: String): String =
    Base64.encodeToString(uri.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)

fun decodeUri(encoded: String): String =
    String(Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP))

@Composable
fun MarkdownMermaidApp(
    intentUri: String? = null,
    isDarkTheme: Boolean = false,
    onThemeChanged: (Boolean) -> Unit = {},
    isTableFormat: Boolean = false,
    onTableFormatChanged: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()

    // Intent URI가 있으면 home에서 시작 후 즉시 viewer로 이동 (back stack에 home 유지)
    LaunchedEffect(intentUri) {
        if (intentUri != null) {
            navController.navigate("viewer/${encodeUri(intentUri)}")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeRoute(
                onDocumentClick = { doc ->
                    navController.navigate("viewer/${encodeUri(doc.uriString)}")
                },
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        composable(
            route = "viewer/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri")?.let { decodeUri(it) } ?: ""
            ViewerRoute(
                uriString = uriString,
                isDark = isDarkTheme,
                isTableFormat = isTableFormat,
                onBackClick = { navController.popBackStack() },
                onNavigateToDocument = { linkedUri ->
                    navController.navigate("viewer/${encodeUri(linkedUri)}")
                }
            )
        }

        composable("settings") {
            SettingsRoute(
                isDarkTheme = isDarkTheme,
                onThemeChanged = onThemeChanged,
                isTableFormat = isTableFormat,
                onTableFormatChanged = onTableFormatChanged,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

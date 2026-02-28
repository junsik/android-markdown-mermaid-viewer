package com.github.junsik.markdown.mermaid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var repository: com.github.junsik.markdown.mermaid.domain.repository.DocumentRepository

    // 현재 처리 중인 URI와 처리 여부를 관리
    private var intentUriState = mutableStateOf<String?>(null)
    private var isIntentProcessed = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isTableFormat by settingsViewModel.isTableFormat.collectAsStateWithLifecycle()
            val currentIntentUri by intentUriState
            val processed by isIntentProcessed

            PremiumTheme(darkTheme = isDarkTheme) {
                MarkdownMermaidApp(
                    intentUri = if (!processed) currentIntentUri else null,
                    onIntentHandled = { isIntentProcessed.value = true },
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = settingsViewModel::onThemeChanged,
                    isTableFormat = isTableFormat,
                    onTableFormatChanged = settingsViewModel::onTableFormatChanged
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null) {
            // 새로운 인텐트가 오면 초기화
            isIntentProcessed.value = false
            
            val flags = intent.flags
            if ((flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // 모든 예외 방어
                }
            }

            // [정석 해결] Activity context의 권한이 살아있을 때 즉시 내부 저장소로 복사
            lifecycleScope.launch {
                val cachedUri = repository.cacheExternalFile(uri.toString())
                intentUriState.value = cachedUri
            }
        }
    }
}

/** 내비게이션용 안전 인코딩 */
fun encodeUriForRoute(uri: String): String = Uri.encode(uri)
fun decodeUriFromRoute(encoded: String): String = Uri.decode(encoded)

@Composable
fun MarkdownMermaidApp(
    intentUri: String? = null,
    onIntentHandled: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onThemeChanged: (Boolean) -> Unit = {},
    isTableFormat: Boolean = false,
    onTableFormatChanged: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    
    // 인텐트 진입 여부 결정
    val startDest = if (intentUri != null) "home" else "splash"

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable("splash") {
            SplashScreen(onAnimationFinish = {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("home") {
            // 인텐트로 넘어온 URI가 있으면 즉시 뷰어로 이동
            LaunchedEffect(intentUri) {
                if (intentUri != null) {
                    val encoded = encodeUriForRoute(intentUri)
                    navController.navigate("viewer/$encoded") {
                        // 홈 스택은 유지하되 중복 이동 방지
                        launchSingleTop = true
                    }
                    onIntentHandled() // 처리 완료 보고
                }
            }

            HomeRoute(
                onDocumentClick = { doc ->
                    val encoded = encodeUriForRoute(doc.uriString)
                    navController.navigate("viewer/$encoded")
                },
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        composable(
            route = "viewer/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("uri") ?: ""
            val uriString = try {
                decodeUriFromRoute(encodedUri)
            } catch (e: Exception) {
                ""
            }

            if (uriString.isNotEmpty()) {
                ViewerRoute(
                    uriString = uriString,
                    isDark = isDarkTheme,
                    isTableFormat = isTableFormat,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToDocument = { linkedUri ->
                        val encoded = encodeUriForRoute(linkedUri)
                        navController.navigate("viewer/$encoded")
                    }
                )
            }
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

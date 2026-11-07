package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import android.content.Context
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthManager
import com.example.ui.splash.SplashScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.editor.VideoEditorScreen
import com.example.ui.recordings.RecordingsScreen
import com.example.ui.recordings.VideoPlayerDialog
import com.example.ui.scripts.ScriptsScreen
import com.example.ui.studio.StudioScreen
import com.example.ui.studio.StudioViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WiseBodyFontFamily
import com.example.ui.theme.WiseDisplayFontFamily
import java.io.File

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    AUTH,
    STUDIO,
    SCRIPTS,
    RECORDINGS,
    EDITOR
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppNavHost()
            }
        }
    }
}

@Composable
fun MainAppNavHost(viewModel: StudioViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { AuthManager(context.applicationContext, coroutineScope) }
    val currentUser by authManager.currentUser.collectAsState()

    val appPrefs = remember { context.getSharedPreferences("teleprompt_app_prefs", Context.MODE_PRIVATE) }
    var hasCompletedOnboarding by remember {
        mutableStateOf(appPrefs.getBoolean("has_completed_onboarding", false))
    }

    var currentScreen by remember {
        mutableStateOf(AppScreen.SPLASH)
    }

    var activePreviewVideoPath by remember { mutableStateOf<String?>(null) }
    var editorVideoPath by remember { mutableStateOf("") }
    var editorVideoTitle by remember { mutableStateOf("") }

    // Check camera & mic permissions
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
    }

    LaunchedEffect(Unit) {
        authManager.attemptAutoSignIn()
        if (hasCompletedOnboarding && (!hasCameraPermission || !hasAudioPermission)) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    val shareVideo: (String) -> Unit = { path ->
        try {
            val file = File(path)
            if (file.exists()) {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/mp4"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Recorded with TelePrompt")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Video Take"))
            } else {
                Toast.makeText(context, "Video file not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_navigation"
        ) { screen ->
            when (screen) {
                AppScreen.SPLASH -> {
                    SplashScreen(
                        onSplashFinished = {
                            currentScreen = if (!hasCompletedOnboarding) {
                                AppScreen.ONBOARDING
                            } else if (currentUser == null) {
                                AppScreen.AUTH
                            } else {
                                AppScreen.STUDIO
                            }
                        }
                    )
                }
                AppScreen.ONBOARDING -> {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            appPrefs.edit().putBoolean("has_completed_onboarding", true).apply()
                            hasCompletedOnboarding = true
                            currentScreen = AppScreen.AUTH
                        }
                    )
                }
                AppScreen.AUTH -> {
                    AuthScreen(
                        authManager = authManager,
                        onAuthSuccess = {
                            currentScreen = AppScreen.STUDIO
                        },
                        onBack = if (hasCompletedOnboarding && currentUser != null) {
                            { currentScreen = AppScreen.STUDIO }
                        } else null
                    )
                }
                AppScreen.STUDIO -> {
                    if (!hasCameraPermission) {
                        // Permission Rationale Card in M3 Light Theme
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant),
                                shadowElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = com.example.ui.theme.TorqueBlueContainer,
                                        border = androidx.compose.foundation.BorderStroke(2.dp, com.example.ui.theme.TorqueBlueAccent),
                                        modifier = Modifier.size(76.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Videocam,
                                                contentDescription = null,
                                                tint = com.example.ui.theme.TorqueBluePrimary,
                                                modifier = Modifier.size(38.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Text(
                                        text = "Camera & Audio Access",
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = WiseDisplayFontFamily,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "TelePrompt records your front-facing short videos while scrolling your scripts in real time. Please grant camera and audio permissions to start recording.",
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp,
                                        fontFamily = WiseBodyFontFamily,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(26.dp))

                                    Button(
                                        onClick = {
                                            permissionLauncher.launch(
                                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.TorqueBluePrimary),
                                        shape = RoundedCornerShape(9999.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("grant_permissions_button")
                                    ) {
                                        Text(
                                            text = "Grant Permissions",
                                            color = com.example.ui.theme.TorqueBlueOnPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = WiseBodyFontFamily,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        StudioScreen(
                            viewModel = viewModel,
                            onNavigateToScripts = { currentScreen = AppScreen.SCRIPTS },
                            onNavigateToRecordings = { currentScreen = AppScreen.RECORDINGS },
                            onNavigateToEditor = { path, title ->
                                editorVideoPath = path
                                editorVideoTitle = title
                                currentScreen = AppScreen.EDITOR
                            },
                            onPlayRecording = { path -> activePreviewVideoPath = path },
                            onShareRecording = shareVideo,
                            authManager = authManager,
                            onNavigateToAuth = { currentScreen = AppScreen.AUTH },
                            onReplayOnboarding = { currentScreen = AppScreen.ONBOARDING }
                        )
                    }
                }
                AppScreen.SCRIPTS -> {
                        ScriptsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = AppScreen.STUDIO },
                            onSelectAndRecord = { script ->
                                currentScreen = AppScreen.STUDIO
                            }
                        )
                    }
                    AppScreen.RECORDINGS -> {
                        RecordingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = AppScreen.STUDIO },
                            onOpenEditor = { path, title ->
                                editorVideoPath = path
                                editorVideoTitle = title
                                currentScreen = AppScreen.EDITOR
                            },
                            onShareRecording = shareVideo
                        )
                    }
                    AppScreen.EDITOR -> {
                        VideoEditorScreen(
                            videoFilePath = editorVideoPath,
                            videoTitle = editorVideoTitle,
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = AppScreen.STUDIO }
                        )
                    }
                }
            }

            // Global Video Player Modal
            activePreviewVideoPath?.let { path ->
                VideoPlayerDialog(
                    filePath = path,
                    onDismiss = { activePreviewVideoPath = null },
                    onShare = { shareVideo(path) }
                )
            }
        }
    }

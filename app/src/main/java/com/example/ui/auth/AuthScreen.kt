package com.example.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.ui.theme.TorqueBluePrimary
import com.example.ui.theme.TorqueBlueAccent
import com.example.ui.theme.TorqueBlueContainer
import com.example.ui.theme.TorqueBlueOnContainer
import com.example.ui.theme.TorqueBlueOnPrimary
import com.example.ui.theme.TorqueBlueSecondary
import com.example.ui.theme.WiseBodyFontFamily
import com.example.ui.theme.WiseDisplayFontFamily
import com.example.ui.util.scaleOnPress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authManager: AuthManager,
    onAuthSuccess: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isLoading by authManager.isLoading.collectAsState()
    val authError by authManager.authError.collectAsState()

    var isSignUpTab by remember { mutableStateOf(false) }

    // Dialog state for Phone OTP
    var showPhoneDialog by remember { mutableStateOf(false) }
    var phoneNumberInput by remember { mutableStateOf("") }
    var phoneOtpInput by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }

    // Email Input state
    var showEmailForm by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = statusBarPadding + 16.dp,
                    bottom = navBarPadding + 24.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("auth_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Guest / Skip exploration button
                TextButton(
                    onClick = {
                        authManager.continueAsGuest(onSuccess = onAuthSuccess)
                    },
                    modifier = Modifier.testTag("auth_guest_button")
                ) {
                    Text(
                        "Skip for Now",
                        color = TorqueBluePrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = WiseBodyFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Branding Glaze 3D Clapperboard Emblem
            Surface(
                shape = CircleShape,
                color = TorqueBlueContainer,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, TorqueBlueAccent.copy(alpha = 0.5f)),
                modifier = Modifier.size(86.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_glaze_clapper_1793986841462),
                        contentDescription = "TelePrompt Studio 3D Clapperboard",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSignUpTab) "Create Creator Account" else "Welcome to TelePrompt",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                fontFamily = WiseDisplayFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isSignUpTab) "Sign up to sync your scripts & recordings across devices" else "Sign in to save takes, scripts, and export to social reels",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontFamily = WiseBodyFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Material 3 Expressive Pill Switcher Tabs (rounded.pill 9999.dp)
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (!isSignUpTab) TorqueBluePrimary else Color.Transparent)
                            .clickable { isSignUpTab = false }
                            .padding(vertical = 11.dp)
                            .testTag("auth_tab_signin")
                    ) {
                        Text(
                            text = "Sign In",
                            color = if (!isSignUpTab) TorqueBlueOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (!isSignUpTab) FontWeight.Bold else FontWeight.SemiBold,
                            fontFamily = WiseBodyFontFamily,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (isSignUpTab) TorqueBluePrimary else Color.Transparent)
                            .clickable { isSignUpTab = true }
                            .padding(vertical = 11.dp)
                            .testTag("auth_tab_signup")
                    ) {
                        Text(
                            text = "Create Account",
                            color = if (isSignUpTab) TorqueBlueOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSignUpTab) FontWeight.Bold else FontWeight.SemiBold,
                            fontFamily = WiseBodyFontFamily,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Error Notice Banner
            if (authError != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { authManager.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TorqueBluePrimary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Authenticating...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // AUTH ACTION BUTTONS (M3 Light Theme)

                // 1. CONTINUE WITH GOOGLE (Primary Credential Manager)
                Button(
                    onClick = {
                        activity?.let {
                            authManager.signInWithGoogle(it, onSuccess = onAuthSuccess, onError = {})
                        }
                    },
                    shape = RoundedCornerShape(9999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scaleOnPress(0.96f)
                        .testTag("auth_google_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌐", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isSignUpTab) "Sign up with Google" else "Continue with Google",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseBodyFontFamily,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. CONTINUE WITH APPLE ID
                Button(
                    onClick = {
                        authManager.signInWithApple(onSuccess = onAuthSuccess, onError = {})
                    },
                    shape = RoundedCornerShape(9999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161A14)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3228)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scaleOnPress(0.96f)
                        .testTag("auth_apple_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isSignUpTab) "Sign up with Apple ID" else "Sign in with Apple ID",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseBodyFontFamily,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. CONTINUE WITH PHONE NUMBER
                Button(
                    onClick = {
                        otpSent = false
                        phoneOtpInput = ""
                        showPhoneDialog = true
                    },
                    shape = RoundedCornerShape(9999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scaleOnPress(0.96f)
                        .testTag("auth_phone_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone Number",
                            tint = TorqueBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isSignUpTab) "Sign up with Phone Number" else "Continue with Phone Number",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = WiseBodyFontFamily,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OR Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = "OR",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = WiseBodyFontFamily,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. CONTINUE WITH EMAIL PROVIDER (Toggle Form)
                if (!showEmailForm) {
                    OutlinedButton(
                        onClick = { showEmailForm = true },
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .scaleOnPress(0.96f)
                            .testTag("auth_email_toggle_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TorqueBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isSignUpTab) "Sign up with Email" else "Continue with Email & Password",
                                fontSize = 14.sp,
                                fontFamily = WiseBodyFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    // Email & Password Fields
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (isSignUpTab) "Create with Email" else "Sign in with Email",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontFamily = WiseDisplayFontFamily,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = TorqueBluePrimary)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TorqueBluePrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedLabelColor = TorqueBluePrimary,
                                    cursorColor = TorqueBluePrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_email_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password") },
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = TorqueBluePrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TorqueBluePrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedLabelColor = TorqueBluePrimary,
                                    cursorColor = TorqueBluePrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_password_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    authManager.signInWithEmail(
                                        email = emailInput,
                                        isSignUp = isSignUpTab,
                                        onSuccess = onAuthSuccess,
                                        onError = {}
                                    )
                                },
                                enabled = emailInput.isNotBlank() && passwordInput.length >= 6,
                                shape = RoundedCornerShape(9999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TorqueBluePrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auth_email_submit_button")
                            ) {
                                Text(
                                    text = if (isSignUpTab) "Create Account" else "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = WiseBodyFontFamily,
                                    color = TorqueBlueOnPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Terms & Privacy statement
            Text(
                text = "By signing in, you agree to TelePrompt's Terms of Service and Privacy Policy. Your takes are securely saved.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = WiseBodyFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }

    // Modal Bottom Sheet: Phone OTP Flow in M3 Light Theme
    if (showPhoneDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPhoneDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (otpSent) "Enter Verification Code" else "Phone Authentication",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = WiseDisplayFontFamily
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (otpSent) "We sent a 6-digit verification code to $phoneNumberInput" else "Enter your mobile phone number with country code (e.g. +1 555 123 4567)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontFamily = WiseBodyFontFamily
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (!otpSent) {
                    OutlinedTextField(
                        value = phoneNumberInput,
                        onValueChange = { phoneNumberInput = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+1 555 000 0000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = TorqueBluePrimary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TorqueBluePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = TorqueBluePrimary,
                            cursorColor = TorqueBluePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_phone_input")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            otpSent = true
                        },
                        enabled = phoneNumberInput.isNotBlank() && phoneNumberInput.length >= 8,
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TorqueBluePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_send_otp_button")
                    ) {
                        Text(
                            "Send Verification Code",
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseBodyFontFamily,
                            color = TorqueBlueOnPrimary
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = phoneOtpInput,
                        onValueChange = { if (it.length <= 6) phoneOtpInput = it },
                        label = { Text("6-Digit OTP Code") },
                        placeholder = { Text("123456") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TorqueBluePrimary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TorqueBluePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = TorqueBluePrimary,
                            cursorColor = TorqueBluePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_otp_input")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            authManager.signInWithPhone(
                                phoneNumber = phoneNumberInput,
                                smsCode = phoneOtpInput,
                                onSuccess = {
                                    showPhoneDialog = false
                                    onAuthSuccess()
                                },
                                onError = {}
                            )
                        },
                        enabled = phoneOtpInput.length == 6,
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TorqueBluePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_verify_otp_button")
                    ) {
                        Text(
                            "Verify & Sign In",
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseBodyFontFamily,
                            color = TorqueBlueOnPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthProviderType
import com.example.auth.UserProfile
import com.example.ui.theme.FantaGreen
import com.example.ui.theme.FantaOrange
import com.example.ui.theme.OnFantaOrange
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioIndigo
import com.example.ui.theme.StudioRecordRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WiseBodyFontFamily
import com.example.ui.theme.WiseDisplayFontFamily

@Composable
fun UserProfileDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        shape = RoundedCornerShape(24.dp),
        containerColor = StudioCardBg,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account Profile",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = WiseDisplayFontFamily
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Icon with Fanta Orange & Green Glow
                Surface(
                    shape = CircleShape,
                    color = FantaOrange.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, FantaGreen),
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (user.provider) {
                                AuthProviderType.GOOGLE -> "🌐"
                                AuthProviderType.APPLE -> ""
                                AuthProviderType.PHONE -> "📱"
                                AuthProviderType.EMAIL -> "✉️"
                                AuthProviderType.GUEST -> "👤"
                            },
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = user.displayName,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = WiseDisplayFontFamily
                )

                if (user.email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontFamily = WiseBodyFontFamily
                    )
                }

                if (user.phoneNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.phoneNumber,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontFamily = WiseBodyFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Wise Pill Provider Badge
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = FantaGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FantaGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Signed in with ${user.provider.name}",
                        color = FantaGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = WiseBodyFontFamily,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (user.isGuest) {
                    Button(
                        onClick = {
                            onDismiss()
                            onNavigateToAuth()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FantaOrange),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Create Full Account / Link",
                            color = OnFantaOrange,
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseBodyFontFamily
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Sign Out Button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onSignOut()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioRecordRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioRecordRed.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = StudioRecordRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        color = StudioRecordRed,
                        fontWeight = FontWeight.Bold,
                        fontFamily = WiseBodyFontFamily
                    )
                }
            }
        }
    )
}

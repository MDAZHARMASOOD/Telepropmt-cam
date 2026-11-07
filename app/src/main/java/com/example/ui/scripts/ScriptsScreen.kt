package com.example.ui.scripts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Script
import com.example.ui.studio.StudioViewModel
import com.example.ui.theme.TorqueBluePrimary
import com.example.ui.theme.TorqueBlueContainer
import com.example.ui.theme.TorqueBlueOnContainer
import com.example.ui.theme.TorqueBlueAccent
import com.example.ui.theme.TorqueLightSurface
import com.example.ui.theme.TorqueLightSurfaceContainer
import com.example.ui.theme.TorqueLightSurfaceContainerLow
import com.example.ui.theme.TorqueLightSurfaceContainerHigh
import com.example.ui.theme.TorqueLightOnSurface
import com.example.ui.theme.TorqueLightOnSurfaceVariant
import com.example.ui.theme.TorqueLightOutline
import com.example.ui.theme.TorqueLightOutlineVariant
import com.example.ui.theme.StudioRecordRed
import com.example.ui.theme.ChationYellow
import com.example.ui.theme.ChationDark
import com.example.ui.theme.WiseBodyFontFamily
import com.example.ui.theme.WiseDisplayFontFamily
import androidx.compose.material.icons.filled.Description
import com.example.ui.util.scaleOnPress

@Composable
fun ScriptsScreen(
    viewModel: StudioViewModel,
    onNavigateBack: () -> Unit,
    onSelectAndRecord: (Script) -> Unit
) {
    BackHandler { onNavigateBack() }

    val allScripts by viewModel.allScripts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedScriptTab by remember { mutableStateOf(0) } // 0 = Top Scripts, 1 = New Scripts
    var scriptBeingEdited by remember { mutableStateOf<Script?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }

    val filteredScripts = remember(allScripts, searchQuery) {
        if (searchQuery.isBlank()) {
            allScripts
        } else {
            allScripts.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isCreatingNew = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "New Script") },
                text = { Text("New Script", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .scaleOnPress(0.96f)
                    .testTag("create_script_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header: Back, Title & Upload Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .scaleOnPress(0.96f)
                            .testTag("scripts_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to camera studio",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column {
                        Text(
                            text = "Script Library",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${allScripts.size} scripts available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // Upload Script Button (Material 3 Outlined Pill)
                OutlinedButton(
                    onClick = { showUploadDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .scaleOnPress(0.96f)
                        .testTag("upload_script_header_button")
                ) {
                    Text("📁 Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Material 3 Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search scripts by title or text...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("search_scripts_input")
            )

            // Segmented Pill Tabs matching reference image ("Top Chatbots" / "New Chatbots")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFFE5E7EB).copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (selectedScriptTab == 0) ChationYellow else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedScriptTab = 0 }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Top Scripts",
                            color = if (selectedScriptTab == 0) ChationDark else Color(0xFF4B5563),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseBodyFontFamily
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (selectedScriptTab == 1) ChationYellow else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedScriptTab = 1 }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "New Scripts",
                            color = if (selectedScriptTab == 1) ChationDark else Color(0xFF4B5563),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = WiseBodyFontFamily
                        )
                    }
                }
            }

            // Scripts List
            if (filteredScripts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isBlank()) "No scripts saved yet" else "No matching scripts found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { isCreatingNew = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(9999.dp),
                            modifier = Modifier.scaleOnPress(0.96f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create First Script", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredScripts, key = { it.id }) { script ->
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("script_card_${script.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Dark Circular Icon Badge matching reference image!
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(ChationDark)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = ChationYellow,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = script.title,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = WiseDisplayFontFamily
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Surface(
                                                shape = RoundedCornerShape(9999.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${script.wordCount} words • ~${script.estimatedDurationSec}s",
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { scriptBeingEdited = script },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .scaleOnPress(0.96f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit script",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteScript(script) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .scaleOnPress(0.96f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete script",
                                                tint = StudioRecordRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = script.content,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 19.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Material 3 Primary CTA Pill Button (Torque Blue)
                                Button(
                                    onClick = {
                                        viewModel.selectScript(script)
                                        onSelectAndRecord(script)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(9999.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .scaleOnPress(0.96f)
                                        .testTag("record_with_script_${script.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Shoot Video with this Script",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Script Editor Dialog (for Creating or Editing in M3 Light Theme)
    if (isCreatingNew || scriptBeingEdited != null) {
        val targetScript = scriptBeingEdited
        ScriptEditorDialog(
            initialTitle = targetScript?.title ?: "",
            initialContent = targetScript?.content ?: "",
            onDismiss = {
                isCreatingNew = false
                scriptBeingEdited = null
            },
            onSave = { title, content ->
                viewModel.saveScript(
                    title = title,
                    content = content,
                    existingId = targetScript?.id ?: 0L
                )
                isCreatingNew = false
                scriptBeingEdited = null
            }
        )
    }

    // Upload Script Dialog (Google Drive & Local Files)
    if (showUploadDialog) {
        UploadScriptDialog(
            viewModel = viewModel,
            onDismiss = { showUploadDialog = false },
            onScriptUploaded = { uploadedScript ->
                viewModel.selectScript(uploadedScript)
            }
        )
    }
}

@Composable
fun ScriptEditorDialog(
    initialTitle: String,
    initialContent: String,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }

    val wordCount = remember(content) {
        content.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
    }
    val estimatedSec = if (wordCount == 0) 0 else ((wordCount / 130f) * 60).toInt().coerceAtLeast(1)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = if (initialTitle.isBlank()) "New Teleprompter Script" else "Edit Script",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Script Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("script_editor_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Content input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Prompter Script Text") },
                    minLines = 8,
                    maxLines = 14,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("script_editor_content_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Word count & Duration stats (Material 3 chip pills)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$wordCount words",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "Estimated: ${estimatedSec}s (~130 WPM)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: M3 TextButton & Filled Pill Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(9999.dp)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (content.isNotBlank()) {
                                onSave(title.ifBlank { "Take Script" }, content)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(9999.dp),
                        enabled = content.isNotBlank(),
                        modifier = Modifier
                            .scaleOnPress(0.96f)
                            .testTag("save_script_button")
                    ) {
                        Text("Save Script", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

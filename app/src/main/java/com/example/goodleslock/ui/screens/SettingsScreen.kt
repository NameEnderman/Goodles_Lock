package com.example.goodleslock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goodleslock.ui.components.HeaderBar
import com.example.goodleslock.utils.Localization
import com.example.goodleslock.utils.OneUiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit,
    manualOneUiVersion: String,
    onOneUiVersionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = remember(currentLanguage) { Localization.getStrings(currentLanguage) }
    val detectedVersion = remember { OneUiUtils.getOneUiVersionString() }
    val oneUiOptions = listOf("4.0", "4.1", "5.0", "5.1", "6.0", "6.1", "7.0")

    val languages = mapOf(
        "en" to "English",
        "ru" to "Русский",
        "uk" to "Українська",
        "es" to "Español",
        "de" to "Deutsch",
        "zh" to "中文",
        "fr" to "Français"
    )

    var oneUiExpanded by remember { mutableStateOf(false) }
    var langExpanded by remember { mutableStateOf(false) }

    var selectedVersion by remember { mutableStateOf(manualOneUiVersion) }
    var pendingVersion by remember { mutableStateOf<String?>(null) }
    var showWarningDialog by remember { mutableStateOf(false) }

    // Warning Dialog for One UI version change
    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = {
                showWarningDialog = false
                pendingVersion = null
            },
            title = {
                Text(
                    text = strings.warningTitle,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = strings.warningText,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingVersion?.let {
                            selectedVersion = it
                            onOneUiVersionChanged(it)
                        }
                        showWarningDialog = false
                        pendingVersion = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(strings.apply, fontFamily = FontFamily.SansSerif)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showWarningDialog = false
                        pendingVersion = null
                    }
                ) {
                    Text(strings.cancel, fontFamily = FontFamily.SansSerif)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeaderBar()

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = strings.settings,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.languageTitle,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = langExpanded,
                        onExpandedChange = { langExpanded = !langExpanded }
                    ) {
                        OutlinedTextField(
                            value = languages[currentLanguage] ?: "English",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.languageTitle) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = langExpanded,
                            onDismissRequest = { langExpanded = false }
                        ) {
                            languages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name, fontFamily = FontFamily.SansSerif) },
                                    onClick = {
                                        langExpanded = false
                                        onLanguageChanged(code)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // One UI selection card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.oneUiVersionTitle,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${strings.detected}: $detectedVersion",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = oneUiExpanded,
                        onExpandedChange = { oneUiExpanded = !oneUiExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedVersion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.selectOneUi) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = oneUiExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = oneUiExpanded,
                            onDismissRequest = { oneUiExpanded = false }
                        ) {
                            oneUiOptions.forEach { version ->
                                DropdownMenuItem(
                                    text = { Text("One UI $version", fontFamily = FontFamily.SansSerif) },
                                    onClick = {
                                        oneUiExpanded = false
                                        if (version != selectedVersion) {
                                            pendingVersion = version
                                            showWarningDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Theme Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.themeTitle,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.themeDesc,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

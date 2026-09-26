package com.example.goodleslock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goodleslock.model.ModuleModel
import com.example.goodleslock.ui.components.HeaderBar
import com.example.goodleslock.utils.Localization

@Composable
fun InstalledScreen(
    currentLanguage: String,
    installedModules: List<ModuleModel>,
    onOpenModule: (ModuleModel) -> Unit,
    onUninstallModule: (ModuleModel) -> Unit,
    onReinstallModule: (ModuleModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = remember(currentLanguage) { Localization.getStrings(currentLanguage) }

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
                text = strings.installed,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (installedModules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.noModules,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(installedModules) { module ->
                    ModuleCard(
                        module = module,
                        actionButtonText = strings.open,
                        onActionClick = { onOpenModule(module) },
                        secondaryActionButton = {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedButton(
                                    onClick = { onReinstallModule(module) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = strings.reinstall,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 12.sp
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onUninstallModule(module) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = strings.uninstall,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

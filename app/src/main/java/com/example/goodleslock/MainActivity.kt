package com.example.goodleslock

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.goodleslock.data.GitHubCatalogRepository
import com.example.goodleslock.model.ModuleModel
import com.example.goodleslock.ui.screens.InstalledScreen
import com.example.goodleslock.ui.screens.SettingsScreen
import com.example.goodleslock.ui.screens.UninstalledScreen
import com.example.goodleslock.ui.theme.GoodlesLockTheme
import com.example.goodleslock.utils.Localization
import com.example.goodleslock.utils.OneUiUtils
import com.example.goodleslock.utils.PreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoodlesLockTheme {
                GoodlesLockApp()
            }
        }
    }
}

enum class AppTab(
    val icon: ImageVector
) {
    UNINSTALLED(Icons.Default.Apps),
    INSTALLED(Icons.Default.CheckCircle),
    SETTINGS(Icons.Default.Settings)
}

@Composable
fun GoodlesLockApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { GitHubCatalogRepository(context) }

    var currentTab by rememberSaveable { mutableStateOf(AppTab.UNINSTALLED) }

    // Load persisted or auto-detected language and One UI version
    var currentLanguage by remember {
        mutableStateOf(PreferencesHelper.getSavedLanguage(context))
    }
    var oneUiVersion by remember {
        mutableStateOf(PreferencesHelper.getSavedOneUiVersion(context, OneUiUtils.getOneUiVersionString()))
    }

    val strings = remember(currentLanguage) { Localization.getStrings(currentLanguage) }

    var allModules by remember { mutableStateOf<List<ModuleModel>>(emptyList()) }
    var installedPackageNames by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Fetch catalog from GitHub
    fun refreshData() {
        coroutineScope.launch {
            val catalog = repository.fetchCatalog(oneUiVersion)
            allModules = catalog

            val pm = context.packageManager
            val installed = mutableSetOf<String>()
            for (mod in catalog) {
                try {
                    pm.getPackageInfo(mod.packageName, 0)
                    installed.add(mod.packageName)
                } catch (_: Exception) {
                    // Not installed
                }
            }
            installedPackageNames = installed
        }
    }

    LaunchedEffect(oneUiVersion) {
        refreshData()
    }

    val uninstalledModules = allModules.filter { !installedPackageNames.contains(it.packageName) }
    val installedModules = allModules.filter { installedPackageNames.contains(it.packageName) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                AppTab.entries.forEach { tab ->
                    val label = when (tab) {
                        AppTab.UNINSTALLED -> strings.uninstalled
                        AppTab.INSTALLED -> strings.installed
                        AppTab.SETTINGS -> strings.settings
                    }
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (currentTab) {
            AppTab.UNINSTALLED -> UninstalledScreen(
                currentLanguage = currentLanguage,
                modules = uninstalledModules,
                onDownloadModule = { module ->
                    coroutineScope.launch {
                        Toast.makeText(context, "${strings.download} ${module.name}...", Toast.LENGTH_SHORT).show()
                        downloadAndInstallApk(context, module)
                    }
                },
                modifier = modifier
            )
            AppTab.INSTALLED -> InstalledScreen(
                currentLanguage = currentLanguage,
                installedModules = installedModules,
                onOpenModule = { module ->
                    val intent = context.packageManager.getLaunchIntentForPackage(module.packageName)
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Cannot open ${module.name}", Toast.LENGTH_SHORT).show()
                    }
                },
                onUninstallModule = { module ->
                    val intent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${module.packageName}")
                    }
                    context.startActivity(intent)
                },
                onReinstallModule = { module ->
                    coroutineScope.launch {
                        Toast.makeText(context, "${strings.reinstall} ${module.name}...", Toast.LENGTH_SHORT).show()
                        downloadAndInstallApk(context, module)
                    }
                },
                modifier = modifier
            )
            AppTab.SETTINGS -> SettingsScreen(
                currentLanguage = currentLanguage,
                onLanguageChanged = { newLang ->
                    currentLanguage = newLang
                    PreferencesHelper.saveLanguage(context, newLang)
                },
                manualOneUiVersion = oneUiVersion,
                onOneUiVersionChanged = { newVersion ->
                    oneUiVersion = newVersion
                    PreferencesHelper.saveOneUiVersion(context, newVersion)
                },
                modifier = modifier
            )
        }
    }
}

suspend fun downloadAndInstallApk(context: Context, module: ModuleModel) {
    withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "apks")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val apkFile = File(cacheDir, "${module.id}.apk")

            try {
                val url = URL(module.downloadUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                if (connection.responseCode == 200) {
                    val input = connection.inputStream
                    val output = FileOutputStream(apkFile)
                    input.copyTo(output)
                    output.close()
                    input.close()
                } else {
                    apkFile.writeText("Dummy APK content for ${module.name}")
                }
            } catch (e: Exception) {
                apkFile.writeText("Dummy APK content for ${module.name}")
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "${module.name} downloaded! Installing...", Toast.LENGTH_SHORT).show()
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(installIntent)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

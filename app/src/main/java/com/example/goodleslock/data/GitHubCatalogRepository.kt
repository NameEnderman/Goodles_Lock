package com.example.goodleslock.data

import android.content.Context
import android.util.Log
import com.example.goodleslock.model.ModuleModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GitHubCatalogRepository(private val context: Context) {
    private val TAG = "GitHubCatalogRepository"

    // Private GitHub repository raw base URL
    private val baseRepoUrl: String = "https://raw.githubusercontent.com/NameEnderman/goodles_lock.apk/main/"

    suspend fun fetchCatalog(oneUiVersion: String): List<ModuleModel> = withContext(Dispatchers.IO) {
        val folderVersion = oneUiVersion.replace(".", "_")
        val folderName = "one_ui_$folderVersion"
        val targetUrlStr = "${baseRepoUrl}catalog/$folderName/catalog.json"
        
        // 1. Try fetching from GitHub remote repository
        try {
            Log.d(TAG, "Fetching catalog from GitHub: $targetUrlStr")
            val url = URL(targetUrlStr)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
            }

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val parsed = parseCatalogJson(sb.toString())
                if (parsed.isNotEmpty()) {
                    return@withContext parsed
                }
            } else {
                Log.w(TAG, "Failed to fetch remote catalog, response code: ${connection.responseCode}. Trying local assets fallback.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching catalog from GitHub, trying local assets fallback", e)
        }

        // 2. Fallback to local bundled assets catalog matching user's exact catalog.json files
        try {
            val assetPath = "catalog/$folderName/catalog.json"
            Log.d(TAG, "Loading local asset catalog from: $assetPath")
            val inputStream = context.assets.open(assetPath)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()

            val parsed = parseCatalogJson(sb.toString())
            if (parsed.isNotEmpty()) {
                return@withContext parsed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading local asset catalog", e)
        }

        emptyList()
    }

    private fun parseCatalogJson(jsonStr: String): List<ModuleModel> {
        val list = mutableListOf<ModuleModel>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val downloadUrl = obj.optString("downloadUrl", "")
                if (downloadUrl.isNotBlank()) {
                    list.add(
                        ModuleModel(
                            id = obj.optString("id", "mod_$i"),
                            name = obj.optString("name", "Module $i"),
                            description = obj.optString("description", ""),
                            version = obj.optString("version", "1.0.0"),
                            oneUiVersion = obj.optString("oneUiVersion", "6.0"),
                            downloadUrl = downloadUrl,
                            packageName = obj.optString("packageName", "com.example.goodleslock.module$i"),
                            iconUrl = obj.optString("iconUrl", "")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing catalog JSON", e)
        }
        return list
    }
}

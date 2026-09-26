package com.example.goodleslock.utils

import android.content.Context
import java.util.Locale

object PreferencesHelper {
    private const val PREFS_NAME = "goodles_lock_prefs"
    private const val KEY_LANG = "pref_language"
    private const val KEY_ONE_UI = "pref_one_ui_version"
    private const val KEY_THEME = "pref_theme_mode" // "system", "dark", "light"

    private val supportedLanguages = listOf("ru", "uk", "es", "de", "zh", "fr", "en")

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANG, null)
        if (!saved.isNullOrBlank()) {
            return saved
        }
        val systemLang = Locale.getDefault().language.lowercase()
        return if (supportedLanguages.contains(systemLang)) {
            systemLang
        } else {
            "en"
        }
    }

    fun saveLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    fun getSavedOneUiVersion(context: Context, defaultVersion: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ONE_UI, null) ?: defaultVersion
    }

    fun saveOneUiVersion(context: Context, version: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ONE_UI, version).apply()
    }

    fun getSavedThemeMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, null) ?: "system"
    }

    fun saveThemeMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, mode).apply()
    }
}

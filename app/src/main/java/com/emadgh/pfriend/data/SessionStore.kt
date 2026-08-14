package com.emadgh.pfriend.data

import android.content.Context

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("pfriend_session", Context.MODE_PRIVATE)

    var baseUrl: String?
        get() = prefs.getString("base_url", null)
        set(value) { prefs.edit().putString("base_url", value?.normalizeBaseUrl()).apply() }

    var token: String?
        get() = prefs.getString("token", null)
        set(value) { prefs.edit().putString("token", value).apply() }

    fun clearAuth() = prefs.edit().remove("token").apply()
    fun clearAll() = prefs.edit().clear().apply()
}

private fun String.normalizeBaseUrl(): String {
    val trimmed = trim().trimEnd('/')
    return "$trimmed/"
}

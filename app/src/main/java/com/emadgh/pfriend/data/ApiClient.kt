package com.emadgh.pfriend.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ApiException(message: String, val statusCode: Int = 0) : Exception(message)

class ApiClient(private val session: SessionStore) {
    fun get(action: String, params: Map<String, String> = emptyMap()): JSONObject =
        request("GET", action, params, null)

    fun post(action: String, body: JSONObject): JSONObject =
        request("POST", action, emptyMap(), body)

    private fun request(method: String, action: String, params: Map<String, String>, body: JSONObject?): JSONObject {
        val base = session.baseUrl ?: throw ApiException("Server URL is not configured")
        val query = buildList {
            add("action=${encode(action)}")
            params.forEach { (k, v) -> add("${encode(k)}=${encode(v)}") }
        }.joinToString("&")
        val connection = (URL("${base}api.php?$query").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 12_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            session.token?.let { setRequestProperty("Authorization", "Bearer $it") }
            if (method == "POST") doOutput = true
        }

        try {
            if (body != null) connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.use { input -> BufferedReader(InputStreamReader(input)).readText() }.orEmpty()
            val json = if (text.isBlank()) JSONObject() else JSONObject(text)
            if (code !in 200..299 || json.optBoolean("ok", true).not()) {
                throw ApiException(json.optString("error", "Request failed ($code)"), code)
            }
            return json
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(value: String) = java.net.URLEncoder.encode(value, Charsets.UTF_8.name())
}

fun JSONObject.array(name: String): JSONArray = optJSONArray(name) ?: JSONArray()

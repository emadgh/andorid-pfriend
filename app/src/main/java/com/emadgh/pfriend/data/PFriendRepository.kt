package com.emadgh.pfriend.data

import com.emadgh.pfriend.model.*
import org.json.JSONObject

class PFriendRepository(private val session: SessionStore) {
    private val api get() = ApiClient(session)

    fun ping(): String = api.get("server_info").optString("name", "PFriend")

    fun register(username: String, email: String, displayName: String, password: String): User {
        val result = api.post("register", JSONObject()
            .put("username", username).put("email", email).put("display_name", displayName)
            .put("password", password).put("accept_visibility", true))
        session.token = result.getString("token")
        return result.getJSONObject("user").toUser()
    }

    fun login(identity: String, password: String): User {
        val result = api.post("login", JSONObject().put("identity", identity).put("password", password))
        session.token = result.getString("token")
        return result.getJSONObject("user").toUser()
    }

    fun me(): User = api.get("me").getJSONObject("user").toUser()

    fun daily(userId: Long? = null): DailySummary {
        val params = userId?.let { mapOf("user_id" to it.toString()) } ?: emptyMap()
        val o = api.get("daily", params).getJSONObject("summary")
        return DailySummary(
            waterMl = o.optDouble("water_ml", 0.0), foodGrams = o.optDouble("food_g", 0.0),
            urineCount = o.optInt("urine_count", 0), urineMl = o.optDouble("urine_ml", 0.0),
            bowelCount = o.optInt("bowel_count", 0)
        )
    }

    fun recentEntries(userId: Long? = null, limit: Int = 30): List<Entry> {
        val p = mutableMapOf("limit" to limit.toString())
        userId?.let { p["user_id"] = it.toString() }
        return api.get("entries", p).array("entries").let { a ->
            (0 until a.length()).map { i -> a.getJSONObject(i).toEntry() }
        }
    }

    fun addEntry(type: String, amount: Double?, unit: String?, label: String?, note: String?) {
        api.post("entry_add", JSONObject().put("type", type).apply {
            if (amount != null) put("amount", amount)
            if (!unit.isNullOrBlank()) put("unit", unit)
            if (!label.isNullOrBlank()) put("label", label)
            if (!note.isNullOrBlank()) put("note", note)
        })
    }

    fun users(): List<User> = api.get("users").array("users").let { a ->
        (0 until a.length()).map { i -> a.getJSONObject(i).toUser() }
    }

    fun user(id: Long): User = api.get("user", mapOf("id" to id.toString())).getJSONObject("user").toUser()

    fun toggleFollow(userId: Long): Boolean = api.post("follow_toggle", JSONObject().put("user_id", userId)).getBoolean("following")

    fun compare(circleId: Long? = null): List<ComparisonRow> {
        val params = circleId?.let { mapOf("circle_id" to it.toString()) } ?: emptyMap()
        return api.get("compare", params).array("comparison").let { a ->
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                ComparisonRow(
                    user = User(o.getLong("id"), o.getString("username"), o.getString("display_name")),
                    summary = DailySummary(
                        waterMl = o.optDouble("water_ml", 0.0),
                        foodGrams = o.optDouble("food_g", 0.0),
                        urineCount = o.optInt("urine_count", 0),
                        urineMl = o.optDouble("urine_ml", 0.0),
                        bowelCount = o.optInt("bowel_count", 0)
                    )
                )
            }
        }
    }

    fun circles(): List<Circle> = api.get("circles").array("circles").let { a ->
        (0 until a.length()).map { i -> a.getJSONObject(i).toCircle() }
    }

    fun createCircle(name: String): Circle = api.post("circle_create", JSONObject().put("name", name)).getJSONObject("circle").toCircle()

    fun circle(id: Long): CircleDetail {
        val result = api.get("circle", mapOf("id" to id.toString()))
        val c = result.getJSONObject("circle").toCircle()
        val a = result.array("members")
        return CircleDetail(c, (0 until a.length()).map { i ->
            val m = a.getJSONObject(i); CircleMember(m.getLong("id"), m.getString("username"), m.getString("display_name"))
        })
    }

    fun addCircleMember(circleId: Long, username: String) {
        api.post("circle_add_member", JSONObject().put("circle_id", circleId).put("username", username))
    }
}

private fun JSONObject.toUser() = User(
    id = getLong("id"), username = getString("username"), displayName = getString("display_name"),
    email = optString("email").ifBlank { null }, isFollowing = optBoolean("is_following", false)
)

private fun JSONObject.toEntry() = Entry(
    id = getLong("id"), userId = getLong("user_id"), type = getString("type"),
    amount = if (isNull("amount_value")) null else optDouble("amount_value"), unit = optString("unit").ifBlank { null },
    label = optString("label").ifBlank { null }, note = optString("note").ifBlank { null },
    occurredAt = getString("occurred_at"), username = optString("username").ifBlank { null },
    displayName = optString("display_name").ifBlank { null }
)

private fun JSONObject.toCircle() = Circle(
    id = getLong("id"), name = getString("name"), ownerId = getLong("owner_id"), memberCount = optInt("member_count", 0)
)

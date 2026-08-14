package com.emadgh.pfriend.model

data class User(
    val id: Long,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val isFollowing: Boolean = false
)

data class Entry(
    val id: Long,
    val userId: Long,
    val type: String,
    val amount: Double?,
    val unit: String?,
    val label: String?,
    val note: String?,
    val occurredAt: String,
    val username: String? = null,
    val displayName: String? = null
)

data class DailySummary(
    val waterMl: Double = 0.0,
    val foodGrams: Double = 0.0,
    val urineCount: Int = 0,
    val urineMl: Double = 0.0,
    val bowelCount: Int = 0
)

data class Circle(
    val id: Long,
    val name: String,
    val ownerId: Long,
    val memberCount: Int = 0
)

data class CircleMember(val id: Long, val username: String, val displayName: String)

data class CircleDetail(val circle: Circle, val members: List<CircleMember>)

data class ComparisonRow(val user: User, val summary: DailySummary)

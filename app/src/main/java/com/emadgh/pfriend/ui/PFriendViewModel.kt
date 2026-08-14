package com.emadgh.pfriend.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emadgh.pfriend.data.PFriendRepository
import com.emadgh.pfriend.data.SessionStore
import com.emadgh.pfriend.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface Destination {
    data object Home : Destination
    data object People : Destination
    data object Circles : Destination
    data class Compare(val circleId: Long? = null, val circleName: String? = null) : Destination
    data object Profile : Destination
    data class QuickLog(val type: String) : Destination
    data class UserDetail(
        val id: Long,
        val sharedKey: SharedMotionKey,
        val back: Destination,
        val previewDisplayName: String = "",
        val previewUsername: String = ""
    ) : Destination
    data class CircleDetail(val id: Long, val previewName: String = "") : Destination
}

class PFriendViewModel(private val session: SessionStore) : ViewModel() {
    private val repo = PFriendRepository(session)

    var serverConfigured by mutableStateOf(session.baseUrl != null); private set
    var authenticated by mutableStateOf(session.token != null); private set
    var currentUser by mutableStateOf<User?>(null); private set
    var destination by mutableStateOf<Destination>(Destination.Home); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    var daily by mutableStateOf(DailySummary()); private set
    var feed by mutableStateOf<List<Entry>>(emptyList()); private set
    var people by mutableStateOf<List<User>>(emptyList()); private set
    var circles by mutableStateOf<List<Circle>>(emptyList()); private set
    var comparison by mutableStateOf<List<ComparisonRow>>(emptyList()); private set
    var selectedUser by mutableStateOf<User?>(null); private set
    var selectedUserDaily by mutableStateOf(DailySummary()); private set
    var selectedUserEntries by mutableStateOf<List<Entry>>(emptyList()); private set
    var selectedCircle by mutableStateOf<CircleDetail?>(null); private set

    init {
        if (serverConfigured && authenticated) restoreSession()
    }

    fun clearError() { error = null }

    fun configureServer(url: String, onDone: () -> Unit = {}) = task {
        require(url.startsWith("https://")) { "Use an HTTPS server URL" }
        val old = session.baseUrl
        session.baseUrl = url
        try {
            repo.ping()
            serverConfigured = true
            onDone()
        } catch (e: Exception) {
            session.baseUrl = old
            throw e
        }
    }

    fun login(identity: String, password: String) = task {
        currentUser = repo.login(identity.trim(), password)
        authenticated = true
        destination = Destination.Home
        loadHomeNow()
    }

    fun register(username: String, email: String, displayName: String, password: String, accepted: Boolean) = task {
        require(accepted) { "You must accept that your logged data is visible to other registered users." }
        currentUser = repo.register(username.trim(), email.trim(), displayName.trim(), password)
        authenticated = true
        destination = Destination.Home
        loadHomeNow()
    }

    fun logout() {
        session.clearAuth()
        authenticated = false
        currentUser = null
        destination = Destination.Home
    }

    fun resetServer() {
        session.clearAll()
        serverConfigured = false
        authenticated = false
        currentUser = null
        destination = Destination.Home
    }

    fun navigate(to: Destination) {
        destination = to
        when (to) {
            Destination.Home -> loadHome()
            Destination.People -> loadPeople()
            Destination.Circles -> loadCircles()
            is Destination.Compare -> loadCompare(to.circleId)
            is Destination.UserDetail -> {
                if (selectedUser?.id != to.id) {
                    selectedUser = null
                    selectedUserDaily = DailySummary()
                    selectedUserEntries = emptyList()
                }
                loadUser(to.id)
            }
            is Destination.CircleDetail -> {
                if (selectedCircle?.circle?.id != to.id) selectedCircle = null
                loadCircle(to.id)
            }
            else -> Unit
        }
    }

    fun loadHome() = task { loadHomeNow() }
    private suspend fun loadHomeNow() {
        if (currentUser == null) currentUser = repo.me()
        daily = repo.daily()
        feed = repo.recentEntries(limit = 20)
    }

    fun loadPeople() = task { people = repo.users() }
    fun loadCircles() = task { circles = repo.circles() }
    fun loadCompare(circleId: Long? = null) = task { comparison = repo.compare(circleId) }

    fun loadUser(id: Long) = task {
        selectedUser = repo.user(id)
        selectedUserDaily = repo.daily(id)
        selectedUserEntries = repo.recentEntries(id, 30)
    }

    fun toggleFollow() = task {
        val user = selectedUser ?: return@task
        val following = repo.toggleFollow(user.id)
        selectedUser = user.copy(isFollowing = following)
        people = people.map { if (it.id == user.id) it.copy(isFollowing = following) else it }
    }

    fun createCircle(name: String) = task {
        require(name.isNotBlank()) { "Circle name is required" }
        repo.createCircle(name.trim())
        circles = repo.circles()
    }

    fun loadCircle(id: Long) = task { selectedCircle = repo.circle(id) }

    fun addCircleMember(username: String) = task {
        val circle = selectedCircle ?: return@task
        require(username.isNotBlank()) { "Username is required" }
        repo.addCircleMember(circle.circle.id, username.trim())
        selectedCircle = repo.circle(circle.circle.id)
    }

    fun addEntry(type: String, amount: Double?, unit: String?, label: String?, note: String?, onDone: () -> Unit) = task {
        repo.addEntry(type, amount, unit, label, note)
        loadHomeNow()
        destination = Destination.Home
        onDone()
    }

    private fun restoreSession() = task {
        try {
            currentUser = repo.me()
            authenticated = true
            loadHomeNow()
        } catch (e: Exception) {
            session.clearAuth()
            authenticated = false
        }
    }

    private fun task(block: suspend () -> Unit) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                withContext(Dispatchers.IO) { block() }
            } catch (t: Throwable) {
                error = t.message ?: "Unexpected error"
            } finally {
                loading = false
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PFriendViewModel(SessionStore(context.applicationContext)) as T
        }
    }
}

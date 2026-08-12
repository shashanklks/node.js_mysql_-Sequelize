package com.khatabook.clone

import android.app.Application
import android.content.Context
import com.khatabook.clone.data.local.SessionStore
import com.khatabook.clone.data.remote.ApiClient
import com.khatabook.clone.data.repository.KhataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class KhatabookApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}

/**
 * Hand rolled dependency container. The app has one store and one repository,
 * so a full DI framework would only add build time.
 */
object ServiceLocator {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var session: SessionStore
        private set

    lateinit var repository: KhataRepository
        private set

    fun init(context: Context) {
        if (::session.isInitialized) return
        session = SessionStore(context.applicationContext)
        repository = KhataRepository(session)

        // Keep the network client in step with whatever is on disk.
        scope.launch { session.baseUrl.collect { ApiClient.baseUrl = it } }
        scope.launch { session.token.collect { ApiClient.token = it } }
    }

    /** Reads the saved session once, for the splash screen's routing decision. */
    suspend fun awaitToken(): String? {
        ApiClient.baseUrl = session.baseUrl.first()
        return session.token.first().also { ApiClient.token = it }
    }
}

package mg.annuaire.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mg.annuaire.app.data.local.AnnuaireDatabase
import mg.annuaire.app.data.remote.NetworkModule
import mg.annuaire.app.data.remote.PhotoCdn
import mg.annuaire.app.data.repository.AnnuaireRepository
import mg.annuaire.app.data.session.SessionStore
import mg.annuaire.app.data.session.SettingsStore
import mg.annuaire.app.data.sync.CatalogSync

class AnnuaireApp : Application() {
    lateinit var repository: AnnuaireRepository
        private set
    lateinit var sessionStore: SessionStore
        private set
    lateinit var settingsStore: SettingsStore
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _syncStatus = MutableStateFlow("Synchronisation…")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        val db = AnnuaireDatabase.getInstance(this)
        val api = NetworkModule.createApi()
        sessionStore = SessionStore(this)
        settingsStore = SettingsStore(this)
        val sync = CatalogSync(this, db.dao(), api, settingsStore)
        val photoCdn = PhotoCdn(this, api, NetworkModule.createHttpClient(), settingsStore)
        repository = AnnuaireRepository(db.dao(), sync, photoCdn)

        appScope.launch { runSync() }
    }

    fun refreshCatalog() {
        appScope.launch { runSync() }
    }

    private suspend fun runSync() {
        _isRefreshing.value = true
        _syncStatus.value = "Synchronisation…"
        try {
            when (val result = repository.syncCatalog()) {
                is CatalogSync.Result.Ok -> {
                    _syncStatus.value =
                        "Catalogue ${result.source} · ${result.communes} communes · ${result.quartiers} quartiers"
                }
                is CatalogSync.Result.Error -> {
                    _syncStatus.value = "Sync échouée : ${result.message}"
                }
            }
        } finally {
            _isRefreshing.value = false
        }
    }
}

val android.content.Context.annuaireApp: AnnuaireApp
    get() = applicationContext as AnnuaireApp

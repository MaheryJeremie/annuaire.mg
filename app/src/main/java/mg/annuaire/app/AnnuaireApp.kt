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
import mg.annuaire.app.data.sync.CatalogSync

class AnnuaireApp : Application() {
    lateinit var repository: AnnuaireRepository
        private set
    lateinit var sessionStore: SessionStore
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncStatus = MutableStateFlow("Synchronisation…")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        val db = AnnuaireDatabase.getInstance(this)
        val api = NetworkModule.createApi()
        val sync = CatalogSync(this, db.dao(), api)
        val photoCdn = PhotoCdn(this, api, NetworkModule.createHttpClient())
        repository = AnnuaireRepository(db.dao(), sync, photoCdn)
        sessionStore = SessionStore(this)

        appScope.launch {
            when (val result = repository.syncCatalog()) {
                is CatalogSync.Result.Ok -> {
                    _syncStatus.value =
                        "Catalogue ${result.source} · ${result.communes} communes · ${result.quartiers} quartiers"
                }
                is CatalogSync.Result.Error -> {
                    _syncStatus.value = "Sync échouée : ${result.message}"
                }
            }
        }
    }

    fun refreshCatalog() {
        appScope.launch {
            _syncStatus.value = "Synchronisation…"
            when (val result = repository.syncCatalog()) {
                is CatalogSync.Result.Ok -> {
                    _syncStatus.value =
                        "Catalogue ${result.source} · ${result.communes} communes · ${result.quartiers} quartiers"
                }
                is CatalogSync.Result.Error -> {
                    _syncStatus.value = "Sync échouée : ${result.message}"
                }
            }
        }
    }
}

val android.content.Context.annuaireApp: AnnuaireApp
    get() = applicationContext as AnnuaireApp

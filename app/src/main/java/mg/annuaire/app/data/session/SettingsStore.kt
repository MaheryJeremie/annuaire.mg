package mg.annuaire.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mg.annuaire.app.data.model.NetworkMode

private val Context.settingsDataStore by preferencesDataStore("settings")

class SettingsStore(private val context: Context) {
    private val keyNetwork = stringPreferencesKey("network_mode")

    val networkMode: Flow<NetworkMode> = context.settingsDataStore.data.map { prefs ->
        runCatching { NetworkMode.valueOf(prefs[keyNetwork] ?: NetworkMode.WIFI_ONLY.name) }
            .getOrDefault(NetworkMode.WIFI_ONLY)
    }

    suspend fun setNetworkMode(mode: NetworkMode) {
        context.settingsDataStore.edit { it[keyNetwork] = mode.name }
    }
}

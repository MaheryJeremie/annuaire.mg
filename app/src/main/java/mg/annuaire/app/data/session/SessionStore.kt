package mg.annuaire.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("session")

data class SessionUser(
    val userId: Long,
    val role: String,
    val nom: String
)

class SessionStore(private val context: Context) {
    private val keyId = longPreferencesKey("user_id")
    private val keyRole = stringPreferencesKey("user_role")
    private val keyNom = stringPreferencesKey("user_nom")

    val session: Flow<SessionUser?> = context.dataStore.data.map { prefs ->
        val id = prefs[keyId] ?: return@map null
        val role = prefs[keyRole] ?: return@map null
        val nom = prefs[keyNom] ?: return@map null
        SessionUser(id, role, nom)
    }

    suspend fun save(userId: Long, role: String, nom: String) {
        context.dataStore.edit { prefs ->
            prefs[keyId] = userId
            prefs[keyRole] = role
            prefs[keyNom] = nom
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

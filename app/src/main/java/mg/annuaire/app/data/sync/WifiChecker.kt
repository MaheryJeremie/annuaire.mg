package mg.annuaire.app.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import mg.annuaire.app.data.model.NetworkMode

object WifiChecker {
    fun allows(context: Context, mode: NetworkMode): Boolean {
        val caps = capabilities(context) ?: return false
        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return false
        val wifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val cellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        return when (mode) {
            NetworkMode.WIFI_ONLY -> wifi
            NetworkMode.CELLULAR_ONLY -> cellular
            NetworkMode.ANY -> true
        }
    }

    fun fallbackLabel(mode: NetworkMode): String = when (mode) {
        NetworkMode.WIFI_ONLY -> "hors Wi‑Fi · catalogue local"
        NetworkMode.CELLULAR_ONLY -> "hors données mobiles · catalogue local"
        NetworkMode.ANY -> "hors ligne · catalogue local"
    }

    fun successLabel(mode: NetworkMode): String = when (mode) {
        NetworkMode.WIFI_ONLY -> "Wi‑Fi · Firebase"
        NetworkMode.CELLULAR_ONLY -> "Mobile · Firebase"
        NetworkMode.ANY -> "Réseau · Firebase"
    }

    private fun capabilities(context: Context): NetworkCapabilities? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return null
        return cm.getNetworkCapabilities(network)
    }
}

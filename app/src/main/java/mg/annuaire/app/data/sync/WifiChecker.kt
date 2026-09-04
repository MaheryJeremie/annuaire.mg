package mg.annuaire.app.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object WifiChecker {
    /** Sync catalogue : Wi‑Fi uniquement. */
    fun isWifiConnected(context: Context): Boolean {
        val caps = capabilities(context) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Photos de profil : n’importe quelle connexion internet. */
    fun isOnline(context: Context): Boolean {
        val caps = capabilities(context) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun capabilities(context: Context): NetworkCapabilities? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return null
        return cm.getNetworkCapabilities(network)
    }
}

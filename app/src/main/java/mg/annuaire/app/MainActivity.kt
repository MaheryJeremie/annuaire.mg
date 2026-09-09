package mg.annuaire.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mg.annuaire.app.ui.navigation.AnnuaireNavHost
import mg.annuaire.app.ui.theme.AnnuaireTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnnuaireTheme {
                AnnuaireNavHost()
            }
        }
    }
}

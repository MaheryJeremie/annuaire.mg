package mg.annuaire.app.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.annuaire.app.ui.components.AnnuaireBottomBar
import mg.annuaire.app.ui.components.AnnuaireBrandBar
import mg.annuaire.app.ui.components.HintCard
import mg.annuaire.app.ui.components.MainTab
import mg.annuaire.app.ui.components.SectionLabel
import mg.annuaire.app.ui.components.StepRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onTab: (MainTab) -> Unit
) {
    Scaffold(
        topBar = { AnnuaireBrandBar() },
        bottomBar = { AnnuaireBottomBar(MainTab.Help, onTab) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "annuaire.mg est l’annuaire public des prestataires près de chez vous. Pas besoin de compte pour chercher.",
                style = MaterialTheme.typography.bodyLarge
            )

            SectionLabel("Pour trouver quelqu’un")
            StepRow("1", "Parcourez la liste", "Tous les prestataires s’affichent tout de suite. Faites défiler.")
            StepRow("2", "Filtrez si besoin", "Tapez un métier ou un quartier : des suggestions apparaissent. Vous pouvez aussi n’afficher que les profils certifiés.")
            StepRow("3", "Appelez ou SMS", "Tarifs et disponibilité sont affichés avant de contacter.")

            HintCard(
                title = "Badge « Certifié »",
                body = "Le prestataire a envoyé son CIN (numéro + photos recto/verso). La commune a contrôlé le dossier.",
                icon = Icons.Outlined.VerifiedUser
            )
            HintCard(
                title = "Sans compte",
                body = "La recherche est libre. Un compte sert uniquement aux prestataires, pour publier une fiche.",
                icon = Icons.Outlined.Search
            )
            HintCard(
                title = "Réseau pour la sync",
                body = "Par défaut, le catalogue se met à jour en Wi‑Fi seulement. Dans Compte, vous pouvez choisir les données mobiles, ou n’importe quel réseau. Sinon l’app garde les données déjà sur le téléphone.",
                icon = Icons.Outlined.WifiOff
            )

            SectionLabel("Si vous êtes prestataire")
            HelpBlock(
                "1. Inscrivez-vous avec votre téléphone.",
                "2. Remplissez la fiche : photo, métier, quartiers (tapez pour autocompléter, plusieurs possibles) et autant de tarifs que besoin.",
                "3. Si votre métier n’existe pas, tapez-le : des suggestions apparaissent, ou proposez un nouveau nom. La commune valide le nom.",
                "4. Demandez la vérification : numéro de CIN + photo recto + photo verso.",
                "5. Une fois le dossier accepté, le badge « Certifié » s’affiche sur votre fiche."
            )

            SectionLabel("Questions fréquentes")
            Faq("Dois-je payer ?", "Non. L’annuaire est un service public de proximité.")
            Faq("Pourquoi mon métier n’apparaît pas tout de suite ?", "Un nouveau nom doit être validé par la commune, pour éviter les doublons et les fautes.")
            Faq("Mes photos CIN sont-elles publiques ?", "Non. Seule la commune les voit dans son outil web, pour vérifier votre identité. Les visiteurs ne voient pas le CIN.")
            Faq("La photo de profil est-elle publique ?", "Oui. Elle apparaît sur votre fiche. Les photos de CIN restent réservées à la commune.")
            Faq("Puis-je laisser un avis ?", "Oui. Sur la fiche d’un prestataire, donnez une note de 1 à 5 et un commentaire.")
        }
    }
}

@Composable
private fun HelpBlock(vararg lines: String) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun Faq(q: String, a: String) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(q, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(a, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

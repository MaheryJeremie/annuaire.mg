package mg.annuaire.app.ui.visitor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import mg.annuaire.app.annuaireApp
import mg.annuaire.app.ui.components.AnnuaireBottomBar
import mg.annuaire.app.ui.components.AnnuaireBrandBar
import mg.annuaire.app.ui.components.EmptyState
import mg.annuaire.app.ui.components.FilterSuggestionField
import mg.annuaire.app.ui.components.MainTab
import mg.annuaire.app.ui.components.PrestataireAvatar
import mg.annuaire.app.ui.components.PrestataireCard
import mg.annuaire.app.ui.components.RatingRow
import mg.annuaire.app.ui.components.SectionLabel
import mg.annuaire.app.ui.components.StarPicker
import mg.annuaire.app.ui.components.StatusBadge
import mg.annuaire.app.ui.components.formatAr
import mg.annuaire.app.data.model.Avis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeSearchScreen(
    onOpenDetail: (Long) -> Unit,
    onTab: (MainTab) -> Unit,
    vm: SearchViewModel = viewModel(
        factory = SearchViewModel.factory(
            LocalContext.current.applicationContext.annuaireApp.repository
        )
    )
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val hasExtraFilters = state.selectedMetierId != null || state.selectedQuartierId != null
    var showMoreFilters by remember { mutableStateOf(false) }
    val nameCount = remember(state.quartiers) {
        state.quartiers.groupingBy { it.nom.lowercase() }.eachCount()
    }
    val metierSuggestions = remember(state.metierQuery, state.metiers) {
        val q = state.metierQuery.trim()
        val list = if (q.isEmpty()) state.metiers else state.metiers.filter { it.nom.contains(q, ignoreCase = true) }
        list.take(8).map { it.nom to it.id }
    }
    val quartierSuggestions = remember(state.quartierQuery, state.quartiers, state.communes) {
        val q = state.quartierQuery.trim()
        val list = if (q.isEmpty()) state.quartiers
        else state.quartiers.filter { it.nom.contains(q, ignoreCase = true) }
        list.take(10).map { quartier ->
            val extra = if ((nameCount[quartier.nom.lowercase()] ?: 0) > 1) {
                state.communes.find { it.id == quartier.communeId }?.nom?.let { " · $it" }.orEmpty()
            } else ""
            (quartier.nom + extra) to quartier.id
        }
    }

    Scaffold(
        topBar = { AnnuaireBrandBar() },
        bottomBar = { AnnuaireBottomBar(MainTab.Home, onTab) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Prestataires près de chez vous",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Faites défiler la liste. Un filtre n’est pas obligatoire.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.certifiedOnly,
                        onClick = vm::toggleCertified,
                        label = { Text("Certifiés") }
                    )
                    FilterChip(
                        selected = state.availableOnly,
                        onClick = vm::toggleAvailable,
                        label = { Text("Dispo aujourd’hui") }
                    )
                    FilterChip(
                        selected = showMoreFilters || hasExtraFilters,
                        onClick = { showMoreFilters = !showMoreFilters },
                        label = { Text("Métier & quartier") }
                    )
                }
                if (showMoreFilters) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FilterSuggestionField(
                        label = "Métier",
                        placeholder = "Ex. plombier, coiffeur…",
                        query = state.metierQuery,
                        onQueryChange = vm::updateMetierQuery,
                        suggestions = metierSuggestions,
                        onSelect = vm::pickMetier
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterSuggestionField(
                        label = "Quartier",
                        placeholder = "Ex. Alarobia, Analakely…",
                        query = state.quartierQuery,
                        onQueryChange = vm::updateQuartierQuery,
                        suggestions = quartierSuggestions,
                        onSelect = vm::pickQuartier
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${state.results.size} prestataire(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.results.isEmpty()) {
                item {
                    EmptyState("Aucun prestataire pour ces critères. Essayez un autre filtre ou tous les métiers.")
                }
            } else {
                items(state.results, key = { it.id }) { item ->
                    PrestataireCard(item) { onOpenDetail(item.id) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultsScreen(
    onBack: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    vm: SearchViewModel = viewModel(
        factory = SearchViewModel.factory(
            LocalContext.current.applicationContext.annuaireApp.repository
        )
    )
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val metierLabel = state.metiers.find { it.id == state.selectedMetierId }?.nom ?: "Tous métiers"
    val quartierLabel = state.quartiers.find { it.id == state.selectedQuartierId }?.nom ?: "Tous quartiers"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(metierLabel, fontWeight = FontWeight.SemiBold)
                        Text(
                            quartierLabel,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("${state.results.size} résultat(s)", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.availableOnly,
                        onClick = vm::toggleAvailable,
                        label = { Text("Dispo aujourd’hui") }
                    )
                    FilterChip(
                        selected = state.certifiedOnly,
                        onClick = vm::toggleCertified,
                        label = { Text("Certifiés") }
                    )
                }
            }
            if (state.results.isEmpty()) {
                item { EmptyState("Aucun prestataire pour ces critères. Essayez un quartier voisin ou « Tous les métiers ».") }
            } else {
                items(state.results, key = { it.id }) { item ->
                    PrestataireCard(item) { onOpenDetail(item.id) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    prestataireId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vm: DetailViewModel = viewModel(
        factory = DetailViewModel.factory(context.applicationContext.annuaireApp.repository, prestataireId)
    )
    val detail by vm.detail.collectAsStateWithLifecycle()
    val avis by vm.avis.collectAsStateWithLifecycle()
    val feedback by vm.feedback.collectAsStateWithLifecycle()
    val p = detail?.prestataire
    var auteur by remember { mutableStateOf("") }
    var note by remember { mutableStateOf(5) }
    var commentaire by remember { mutableStateOf("") }

    LaunchedEffect(feedback) {
        if (feedback?.startsWith("Avis") == true) {
            auteur = ""
            commentaire = ""
            note = 5
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fiche prestataire") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (p == null || detail == null) {
            EmptyState("Chargement…")
            return@Scaffold
        }
        val moyenne = if (avis.isEmpty()) null else avis.map { it.note }.average()
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PrestataireAvatar(p.photoPath, p.nom, size = 72.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(p.nom, style = MaterialTheme.typography.headlineSmall)
                    Text(detail!!.metierNom, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    RatingRow(moyenne, avis.size)
                    StatusBadge(p.certificationStatus)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                if (p.disponibleAujourdhui) "Disponible aujourd’hui" else "Indisponible aujourd’hui",
                color = if (p.disponibleAujourdhui) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
            Text("Commune : ${detail!!.communeNom}")
            Text("Quartiers : ${detail!!.quartiers.joinToString(", ").ifBlank { "—" }}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(p.description.ifBlank { "Pas de description." })
            SectionLabel("Tarifs déclarés")
            detail!!.tarifs.forEach { t ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(t.libelle)
                    Text(formatAr(t.montantAr), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Les tarifs sont indicatifs. Confirmez au téléphone avant l’intervention.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${p.telephone}")))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Appeler")
                }
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${p.telephone}")).apply {
                                putExtra("sms_body", "Bonjour, je vous contacte via annuaire.mg.")
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("SMS")
                }
            }

            SectionLabel("Avis (${avis.size})")
            if (avis.isEmpty()) {
                Text(
                    "Soyez le premier à laisser un avis.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                avis.forEach { item ->
                    AvisCard(item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            SectionLabel("Laisser un avis")
            OutlinedTextField(
                value = auteur,
                onValueChange = { auteur = it },
                label = { Text("Votre nom") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Note", style = MaterialTheme.typography.labelLarge)
            StarPicker(note = note, onChange = { note = it })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = commentaire,
                onValueChange = { commentaire = it },
                label = { Text("Commentaire (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(16.dp)
            )
            feedback?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    it,
                    color = if (it.startsWith("Avis")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    vm.submitAvis(auteur, note, commentaire)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Publier l’avis")
            }
        }
    }
}

@Composable
private fun AvisCard(avis: Avis) {
    val date = remember(avis.createdAt) {
        SimpleDateFormat("d MMM yyyy", Locale.FRANCE).format(Date(avis.createdAt))
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(avis.auteurNom, fontWeight = FontWeight.SemiBold)
                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RatingRow(avis.note.toDouble(), 1, showCount = false)
            if (avis.commentaire.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(avis.commentaire, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

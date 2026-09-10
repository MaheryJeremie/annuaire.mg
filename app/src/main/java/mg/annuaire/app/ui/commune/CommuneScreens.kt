package mg.annuaire.app.ui.commune

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import mg.annuaire.app.annuaireApp
import mg.annuaire.app.data.model.Prestataire
import mg.annuaire.app.data.model.PrestataireDetail
import mg.annuaire.app.ui.components.EmptyState
import mg.annuaire.app.ui.components.HintCard
import mg.annuaire.app.ui.components.SectionLabel
import mg.annuaire.app.ui.components.StatusBadge
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommuneDashboardScreen(
    onOpenPending: () -> Unit,
    onOpenMetiers: () -> Unit,
    onBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext.annuaireApp
    val pending by app.repository.observePendingCount().collectAsStateWithLifecycle(initialValue = 0)
    val certified by app.repository.observeCertifiedCount().collectAsStateWithLifecycle(initialValue = 0)
    val pendingMetiers by app.repository.observePendingMetierCount().collectAsStateWithLifecycle(initialValue = 0)
    val session by app.sessionStore.session.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Espace commune")
                        Text(session?.nom ?: "Agent", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            HintCard(
                title = "Votre rôle",
                body = "Contrôler les CIN (identité) et valider les nouveaux noms de métiers proposés par les prestataires."
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("CIN à voir", pending.toString(), Modifier.weight(1f))
                StatCard("Certifiés", certified.toString(), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            StatCard("Métiers à valider", pendingMetiers.toString(), Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOpenPending,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Dossiers CIN")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenMetiers,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Noms de métiers proposés")
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingListScreen(
    onOpenDetail: (Long) -> Unit,
    onBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext.annuaireApp
    val pending by app.repository.observePending().collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dossiers CIN (${pending.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (pending.isEmpty()) {
            Column(Modifier.padding(padding)) {
                EmptyState("Aucun dossier CIN en attente.")
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(pending, key = { it.id }) { p ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(p.id) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(p.nom, fontWeight = FontWeight.SemiBold)
                        Text("${p.telephone} · CIN ${p.cinNumero ?: "—"}", style = MaterialTheme.typography.bodySmall)
                        StatusBadge(p.certificationStatus, showInternalStatus = true)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    prestataireId: Long,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext.annuaireApp
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<PrestataireDetail?>(null) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(prestataireId) {
        detail = app.repository.getDetail(prestataireId)
    }

    val p: Prestataire? = detail?.prestataire

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contrôle CIN") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (p == null) {
            EmptyState("Chargement…")
            return@Scaffold
        }
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(p.nom, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(p.telephone)
            Text("Métier : ${detail?.metierNom}")
            Text("Quartiers : ${detail?.quartiers?.joinToString(", ")}")
            Text("Commune : ${detail?.communeNom}")
            Text("N° CIN : ${p.cinNumero ?: "—"}")
            Text(p.description)
            SectionLabel("Pièces CIN")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                CinPreview("Recto", p.cinRectoPath, Modifier.weight(1f))
                CinPreview("Verso", p.cinVersoPath, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Commentaire (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        scope.launch {
                            app.repository.decideCertification(prestataireId, true, comment)
                            onDone()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Valider") }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            app.repository.decideCertification(prestataireId, false, comment)
                            onDone()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Refuser") }
            }
        }
    }
}

@Composable
private fun CinPreview(label: String, path: String?, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(6.dp))
        if (path.isNullOrBlank()) {
            Text("Photo manquante", color = MaterialTheme.colorScheme.error)
        } else {
            AsyncImage(
                model = File(path),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingMetiersScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext.annuaireApp
    val pending by app.repository.observePendingMetiers().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Métiers proposés") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (pending.isEmpty()) {
            Column(Modifier.padding(padding)) {
                EmptyState("Aucun nouveau métier en attente.")
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Acceptez le nom s’il est clair et non doublon. Il apparaîtra ensuite dans les suggestions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(pending, key = { it.id }) { m ->
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(m.nom, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { scope.launch { app.repository.decideMetier(m.id, true) } },
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Valider") }
                            OutlinedButton(
                                onClick = { scope.launch { app.repository.decideMetier(m.id, false) } },
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Refuser") }
                        }
                    }
                }
            }
        }
    }
}

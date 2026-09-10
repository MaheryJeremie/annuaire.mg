package mg.annuaire.app.ui.provider

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddAPhoto
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import mg.annuaire.app.data.local.CinPhotoStore
import mg.annuaire.app.data.local.PhotoStore
import mg.annuaire.app.data.model.Metier
import mg.annuaire.app.data.model.Prestataire
import mg.annuaire.app.ui.components.DropdownField
import mg.annuaire.app.ui.components.HintCard
import mg.annuaire.app.ui.components.MetierSearchField
import mg.annuaire.app.ui.components.PrestataireAvatar
import mg.annuaire.app.ui.components.RatingRow
import mg.annuaire.app.ui.components.SectionLabel
import mg.annuaire.app.ui.components.StatusBadge
import mg.annuaire.app.ui.components.StepRow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHomeScreen(
    onEditProfile: () -> Unit,
    onCertification: () -> Unit,
    onHelp: () -> Unit,
    onBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext.annuaireApp
    val session by app.sessionStore.session.collectAsStateWithLifecycle(initialValue = null)
    var prestataire by remember { mutableStateOf<Prestataire?>(null) }

    LaunchedEffect(session?.userId) {
        val id = session?.userId ?: return@LaunchedEffect
        prestataire = app.repository.getPrestataireForUser(id)
    }

    val avisFlow = remember(prestataire?.id) {
        prestataire?.id?.let { app.repository.observeAvis(it) } ?: flowOf(emptyList())
    }
    val avis by avisFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Espace prestataire")
                        Text("Votre fiche publique", style = MaterialTheme.typography.bodySmall)
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    PrestataireAvatar(prestataire?.photoPath, prestataire?.nom ?: session?.nom.orEmpty(), size = 64.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(prestataire?.nom ?: session?.nom.orEmpty(), fontWeight = FontWeight.Bold)
                        Text(prestataire?.telephone.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        prestataire?.let { StatusBadge(it.certificationStatus, showInternalStatus = true) }
                    }
                }
            }
            HintCard(
                title = "3 étapes pour être visible et de confiance",
                body = "Fiche complète → photos CIN → validation commune."
            )
            StepRow("1", "Ma fiche", "Photo, métier, quartiers, tarifs, disponibilité.")
            Button(onClick = onEditProfile, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Text("Compléter / modifier ma fiche")
            }
            StepRow("2", "Vérification CIN", "Numéro + photo recto + photo verso. Pas de patente.")
            OutlinedButton(onClick = onCertification, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Text("Envoyer mon CIN à la commune")
            }
            OutlinedButton(onClick = onHelp, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Text("Besoin d’aide ?")
            }
            SectionLabel("Avis reçus")
            if (avis.isEmpty()) {
                Text("Aucun avis pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val moyenne = avis.map { it.note }.average()
                RatingRow(moyenne, avis.size)
                avis.forEach { item ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.auteurNom, fontWeight = FontWeight.SemiBold)
                            RatingRow(item.note.toDouble(), 1, showCount = false)
                            if (item.commentaire.isNotBlank()) {
                                Text(item.commentaire, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProviderProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext.annuaireApp
    val session by app.sessionStore.session.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val metiers by app.repository.observeMetiers().collectAsStateWithLifecycle(initialValue = emptyList())
    val communes by app.repository.observeCommunes().collectAsStateWithLifecycle(initialValue = emptyList())

    var prestataire by remember { mutableStateOf<Prestataire?>(null) }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var metierQuery by remember { mutableStateOf("") }
    var selectedMetier by remember { mutableStateOf<Metier?>(null) }
    var communeId by remember { mutableStateOf<Long?>(1L) }
    var selectedQuartiers by remember { mutableStateOf(setOf<Long>()) }
    var dispo by remember { mutableStateOf(true) }
    var tarifLabel by remember { mutableStateOf("Déplacement") }
    var tarifMontant by remember { mutableStateOf("5000") }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val quartiersFlow = remember(communeId) { app.repository.observeQuartiersByCommune(communeId) }
    val quartiers by quartiersFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(session?.userId, metiers) {
        val userId = session?.userId ?: return@LaunchedEffect
        val p = app.repository.getPrestataireForUser(userId) ?: return@LaunchedEffect
        prestataire = p
        photoPath = p.photoPath
        description = p.description
        communeId = p.communeId
        dispo = p.disponibleAujourdhui
        val m = app.repository.getMetier(p.metierId)
        selectedMetier = m
        metierQuery = m?.nom.orEmpty()
        val detail = app.repository.getDetail(p.id)
        detail?.tarifs?.firstOrNull()?.let {
            tarifLabel = it.libelle
            tarifMontant = it.montantAr.toString()
        }
    }

    LaunchedEffect(quartiers, prestataire) {
        val p = prestataire ?: return@LaunchedEffect
        if (quartiers.isEmpty()) return@LaunchedEffect
        val detail = app.repository.getDetail(p.id)
        selectedQuartiers = detail?.quartiers?.mapNotNull { name ->
            quartiers.find { it.nom == name }?.id
        }?.toSet() ?: selectedQuartiers
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            runCatching { PhotoStore.save(context, uri, "profil", "photo") }
                .onSuccess { photoPath = it }
                .onFailure { error = it.message }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ma fiche") },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Ces informations sont visibles par les habitants. Tapez un métier : des suggestions apparaissent. S’il n’existe pas, proposez-le.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Photo de profil", style = MaterialTheme.typography.labelLarge)
            Text(
                "Cette photo apparaît sur votre fiche. Elle est distincte de votre CIN, qui reste privée.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                ) {
                    PrestataireAvatar(photoPath, prestataire?.nom ?: session?.nom.orEmpty(), size = 84.dp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = {
                        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Outlined.AddAPhoto, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (photoPath.isNullOrBlank()) "Ajouter une photo" else "Changer la photo")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            MetierSearchField(
                query = metierQuery,
                onQueryChange = { metierQuery = it },
                metiers = metiers.filter { it.status != "REJECTED" },
                selected = selectedMetier,
                onSelect = {
                    selectedMetier = it
                    metierQuery = it.nom
                },
                onPropose = { nom ->
                    val uid = session?.userId ?: return@MetierSearchField
                    scope.launch {
                        val result = app.repository.resolveOrProposeMetier(nom, uid)
                        result.onSuccess {
                            selectedMetier = it
                            metierQuery = it.nom
                            error = null
                            message = if (it.status == "PENDING") {
                                "Métier proposé. La commune doit valider le nom."
                            } else null
                        }.onFailure { error = it.message }
                    }
                },
                helper = "Exemple : Plombier, Coiffeur, Mécanicien auto…"
            )
            Spacer(modifier = Modifier.height(10.dp))
            DropdownField(
                label = "Commune",
                options = communes.map { it.nom to it.id },
                selectedId = communeId,
                onSelect = {
                    communeId = it
                    selectedQuartiers = emptySet()
                }
            )
            SectionLabel("Quartiers / fokontany où vous intervenez")
            if (quartiers.isEmpty()) {
                Text("Choisissez d’abord une commune.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quartiers.forEach { q ->
                    FilterChip(
                        selected = q.id in selectedQuartiers,
                        onClick = {
                            selectedQuartiers = if (q.id in selectedQuartiers) {
                                selectedQuartiers - q.id
                            } else {
                                selectedQuartiers + q.id
                            }
                        },
                        label = { Text(q.nom) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Présentation (ce que vous faites)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tarifLabel,
                onValueChange = { tarifLabel = it },
                label = { Text("Libellé tarif") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tarifMontant,
                onValueChange = { tarifMontant = it.filter { c -> c.isDigit() } },
                label = { Text("Montant (Ar)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Disponible aujourd’hui")
                Switch(checked = dispo, onCheckedChange = { dispo = it })
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val current = prestataire ?: return@Button
                    val uid = session?.userId ?: return@Button
                    scope.launch {
                        val metierResult = selectedMetier?.let { Result.success(it) }
                            ?: app.repository.resolveOrProposeMetier(metierQuery, uid)
                        metierResult.onSuccess { metier ->
                            selectedMetier = metier
                            app.repository.saveProviderProfile(
                                prestataire = current.copy(
                                    metierId = metier.id,
                                    communeId = communeId ?: current.communeId,
                                    description = description.trim(),
                                    disponibleAujourdhui = dispo,
                                    photoPath = photoPath
                                ),
                                quartierIds = selectedQuartiers.toList(),
                                tarifs = listOf(tarifLabel to (tarifMontant.toIntOrNull() ?: 0))
                            )
                            error = null
                            message = "Fiche enregistrée."
                            prestataire = app.repository.getPrestataireForUser(uid)
                            photoPath = prestataire?.photoPath
                        }.onFailure {
                            error = it.message
                            message = null
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Enregistrer la fiche")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext.annuaireApp
    val session by app.sessionStore.session.collectAsStateWithLifecycle(initialValue = null)
    val communes by app.repository.observeCommunes().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var prestataire by remember { mutableStateOf<Prestataire?>(null) }
    var communeId by remember { mutableStateOf<Long?>(1L) }
    var cin by remember { mutableStateOf("") }
    var recto by remember { mutableStateOf<String?>(null) }
    var verso by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(session?.userId) {
        prestataire = session?.userId?.let { app.repository.getPrestataireForUser(it) }
        cin = prestataire?.cinNumero.orEmpty()
        communeId = prestataire?.communeId ?: 1L
        recto = prestataire?.cinRectoPath
        verso = prestataire?.cinVersoPath
    }

    val pickRecto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            runCatching { CinPhotoStore.save(context, uri, "recto") }
                .onSuccess { recto = it }
                .onFailure { error = it.message }
        }
    }
    val pickVerso = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            runCatching { CinPhotoStore.save(context, uri, "verso") }
                .onSuccess { verso = it }
                .onFailure { error = it.message }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vérification CIN") },
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
                .verticalScroll(rememberScrollState())
        ) {
            prestataire?.let { StatusBadge(it.certificationStatus, showInternalStatus = true) }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Pour le badge « Certifié », envoyez votre CIN. Les visiteurs ne voient pas ces photos — seulement la commune, dans son outil web.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            DropdownField(
                label = "Commune de vérification",
                options = communes.map { it.nom to it.id },
                selectedId = communeId,
                onSelect = { communeId = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = cin,
                onValueChange = { cin = it },
                label = { Text("Numéro CIN") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            SectionLabel("Photos de la CIN")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                CinSlot("Recto (devant)", recto, Modifier.weight(1f)) {
                    pickRecto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                CinSlot("Verso (derrière)", verso, Modifier.weight(1f)) {
                    pickVerso.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val id = prestataire?.id ?: return@Button
                    val cId = communeId ?: return@Button
                    scope.launch {
                        val result = app.repository.requestCertification(id, cId, cin, recto, verso)
                        result.onSuccess {
                            message = "Dossier envoyé. La commune va examiner votre CIN."
                            error = null
                            prestataire = app.repository.getPrestataireForUser(session!!.userId)
                        }.onFailure {
                            error = it.message
                            message = null
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Envoyer le dossier")
            }
        }
    }
}

@Composable
private fun CinSlot(label: String, path: String?, modifier: Modifier, onPick: () -> Unit) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onPick),
            contentAlignment = Alignment.Center
        ) {
            if (path.isNullOrBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.AddAPhoto, contentDescription = null)
                    Text("Ajouter", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                AsyncImage(
                    model = PhotoStore.coilModel(path),
                    contentDescription = label,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

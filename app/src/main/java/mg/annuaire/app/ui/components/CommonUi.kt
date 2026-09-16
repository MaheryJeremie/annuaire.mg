package mg.annuaire.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import mg.annuaire.app.R
import mg.annuaire.app.data.local.PhotoStore
import mg.annuaire.app.data.model.CertificationStatus
import mg.annuaire.app.data.model.PrestataireListItem
import java.text.NumberFormat
import java.util.Locale

enum class MainTab { Home, Help, Account }

@Composable
fun AnnuaireBottomBar(
    current: MainTab,
    onSelect: (MainTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = current == MainTab.Home,
            onClick = { onSelect(MainTab.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Accueil") }
        )
        NavigationBarItem(
            selected = current == MainTab.Help,
            onClick = { onSelect(MainTab.Help) },
            icon = { Icon(Icons.Default.HelpOutline, contentDescription = null) },
            label = { Text("Aide") }
        )
        NavigationBarItem(
            selected = current == MainTab.Account,
            onClick = { onSelect(MainTab.Account) },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text("Compte") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnuaireBrandBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.logo_annuaire),
                    contentDescription = "Logo annuaire.mg",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("annuaire.mg", fontWeight = FontWeight.Bold)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun PrestataireAvatar(
    photoPath: String?,
    nom: String,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    val initials = nom.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2).joinToString("")
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!photoPath.isNullOrBlank()) {
            AsyncImage(
                model = PhotoStore.coilModel(photoPath),
                contentDescription = nom,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(initials, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

private val StarGold = Color(0xFFF59E0B)

@Composable
fun RatingRow(
    note: Double?,
    nbAvis: Int,
    modifier: Modifier = Modifier,
    showCount: Boolean = true
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (nbAvis == 0 || note == null) {
            Text(
                "Pas encore d’avis",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val filled = note.toInt().coerceIn(0, 5)
            repeat(5) { i ->
                Icon(
                    imageVector = if (i < filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            if (showCount) {
                Text(
                    String.format(Locale.FRANCE, "%.1f (%d)", note, nbAvis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StarPicker(
    note: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        (1..5).forEach { value ->
            Icon(
                imageVector = if (value <= note) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "$value étoiles",
                tint = StarGold,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onChange(value) }
                    .padding(2.dp)
            )
        }
    }
}

@Composable
fun StatusBadge(status: String, showInternalStatus: Boolean = false) {
    val isCertified = status == CertificationStatus.CERTIFIED.name
    if (!isCertified && !showInternalStatus) return

    val (label, container, content) = when (status) {
        CertificationStatus.CERTIFIED.name -> Triple(
            "Certifié",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        CertificationStatus.PENDING.name -> Triple(
            "En attente",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        CertificationStatus.REJECTED.name -> Triple(
            "Refusé",
            MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.error
        )
        else -> Triple(
            "Non certifié",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label) },
        leadingIcon = if (isCertified) {
            { Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = container,
            disabledLabelColor = content,
            disabledLeadingIconContentColor = content
        )
    )
}

@Composable
fun PrestataireCard(
    item: PrestataireListItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            PrestataireAvatar(item.photoPath, item.nom)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nom, style = MaterialTheme.typography.titleMedium)
                Text(
                    item.metierNom,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    listOfNotNull(
                        item.commune.takeIf { it.isNotBlank() },
                        item.quartiers.takeIf { it.isNotBlank() }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                RatingRow(item.noteMoyenne, item.nbAvis)
                StatusBadge(item.certificationStatus)
            }
            item.tarifIndicatif?.let { tarif ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "dès",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatAr(tarif),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

fun formatAr(amount: Int): String {
    val nf = NumberFormat.getIntegerInstance(Locale.FRANCE)
    return "${nf.format(amount)} Ar"
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
    )
}

@Composable
fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HintCard(title: String, body: String, icon: ImageVector? = null) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(4.dp))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun StepRow(number: String, title: String, body: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<Pair<String, Long?>>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.second == selectedId }?.first ?: options.firstOrNull()?.first.orEmpty()
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (text, id) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

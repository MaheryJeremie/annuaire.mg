package mg.annuaire.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mg.annuaire.app.data.model.Metier
import mg.annuaire.app.data.model.MetierStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetierSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    metiers: List<Metier>,
    selected: Metier?,
    onSelect: (Metier) -> Unit,
    onPropose: (String) -> Unit,
    helper: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val suggestions = remember(query, metiers) {
        val q = query.trim()
        if (q.isEmpty()) metiers.take(8)
        else metiers.filter { it.nom.contains(q, ignoreCase = true) }.take(8)
    }
    val exact = metiers.any { it.nom.equals(query.trim(), ignoreCase = true) }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded && (suggestions.isNotEmpty() || query.trim().length >= 3),
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    onQueryChange(it)
                    expanded = true
                },
                label = { Text("Métier") },
                placeholder = { Text("Ex. plombier, coiffeur…") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                suggestions.forEach { m ->
                    val suffix = when (m.status) {
                        MetierStatus.PENDING.name -> " · en validation"
                        MetierStatus.REJECTED.name -> " · refusé"
                        else -> ""
                    }
                    DropdownMenuItem(
                        text = { Text(m.nom + suffix) },
                        onClick = {
                            onSelect(m)
                            expanded = false
                        }
                    )
                }
                if (!exact && query.trim().length >= 3) {
                    DropdownMenuItem(
                        text = { Text("Proposer « ${query.trim()} » à la commune") },
                        onClick = {
                            onPropose(query.trim())
                            expanded = false
                        }
                    )
                }
            }
        }
        helper?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        selected?.takeIf { it.status == MetierStatus.PENDING.name }?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "« ${it.nom} » sera visible dans l’annuaire après validation de la commune.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSuggestionField(
    label: String,
    placeholder: String,
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<Pair<String, Long>>,
    onSelect: (id: Long, label: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded && suggestions.isNotEmpty(), onDismissRequest = { expanded = false }) {
            suggestions.forEach { (text, id) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(id, text)
                        expanded = false
                    }
                )
            }
        }
    }
}

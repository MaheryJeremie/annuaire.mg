package mg.annuaire.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import mg.annuaire.app.annuaireApp
import mg.annuaire.app.data.model.UserRole
import mg.annuaire.app.ui.components.AnnuaireBottomBar
import mg.annuaire.app.ui.components.AnnuaireBrandBar
import mg.annuaire.app.ui.components.HintCard
import mg.annuaire.app.ui.components.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountHubScreen(
    onContinueVisitor: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onOpenProvider: () -> Unit,
    onOpenCommune: () -> Unit,
    onLogout: () -> Unit,
    onTab: (MainTab) -> Unit
) {
    val app = LocalContext.current.applicationContext.annuaireApp
    val session by app.sessionStore.session.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = { AnnuaireBrandBar() },
        bottomBar = { AnnuaireBottomBar(MainTab.Account, onTab) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (session == null) {
                HintCard(
                    title = "Chercher sans compte",
                    body = "L’annuaire est ouvert à tous. Un compte n’est utile que si vous êtes prestataire ou agent de commune.",
                    icon = Icons.Outlined.Search
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Prestataire", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Publiez votre fiche, proposez un métier s’il manque, envoyez votre CIN pour le badge Certifié.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Text("Se connecter")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Text("Créer un compte prestataire")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Agent communal", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Valider les CIN et les nouveaux noms de métiers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Text("Connexion commune")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onContinueVisitor, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text("Retour à la recherche")
                }
            } else {
                HintCard(
                    title = "Connecté : ${session!!.nom}",
                    body = if (session!!.role == UserRole.AGENT.name) {
                        "Vous validez les dossiers CIN et les métiers proposés."
                    } else {
                        "Complétez votre fiche, puis envoyez votre CIN."
                    },
                    icon = Icons.Outlined.VerifiedUser
                )
                Spacer(modifier = Modifier.height(12.dp))
                when (session!!.role) {
                    UserRole.PROVIDER.name -> Button(
                        onClick = onOpenProvider,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Mon espace prestataire") }
                    UserRole.AGENT.name -> Button(
                        onClick = onOpenCommune,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Espace commune") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text("Se déconnecter")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoggedIn: (role: String) -> Unit
) {
    val app = LocalContext.current.applicationContext.annuaireApp
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connexion") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Compte démo agent : 0320000000 / agent123\nCompte démo prestataire : 0341111111 / demo123",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Téléphone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp)
            )
            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                enabled = !loading,
                onClick = {
                    loading = true
                    error = null
                    scope.launch {
                        val result = app.repository.login(phone, password)
                        result.onSuccess { user ->
                            app.sessionStore.save(user.id, user.role, user.nom)
                            onLoggedIn(user.role)
                        }.onFailure {
                            error = it.message
                        }
                        loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (loading) "…" else "Se connecter")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    val app = LocalContext.current.applicationContext.annuaireApp
    val scope = rememberCoroutineScope()
    var nom by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inscription prestataire") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Ensuite : fiche (métier + quartiers) puis envoi du CIN pour la vérification communale.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(nom, { nom = it }, label = { Text("Nom complet") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(phone, { phone = it }, label = { Text("Téléphone") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                password,
                { password = it },
                label = { Text("Mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp)
            )
            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        val result = app.repository.registerProvider(nom, phone, password)
                        result.onSuccess { user ->
                            app.sessionStore.save(user.id, user.role, user.nom)
                            onRegistered()
                        }.onFailure { error = it.message }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Créer mon compte")
            }
        }
    }
}

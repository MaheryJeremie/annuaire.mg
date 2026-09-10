package mg.annuaire.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import mg.annuaire.app.annuaireApp
import mg.annuaire.app.data.model.UserRole
import mg.annuaire.app.ui.auth.AccountHubScreen
import mg.annuaire.app.ui.auth.LoginScreen
import mg.annuaire.app.ui.auth.RegisterScreen
import mg.annuaire.app.ui.components.MainTab
import mg.annuaire.app.ui.help.HelpScreen
import mg.annuaire.app.ui.provider.CertificationScreen
import mg.annuaire.app.ui.provider.ProviderHomeScreen
import mg.annuaire.app.ui.provider.ProviderProfileScreen
import mg.annuaire.app.ui.visitor.DetailScreen
import mg.annuaire.app.ui.visitor.HomeSearchScreen
import mg.annuaire.app.ui.visitor.ResultsScreen
import mg.annuaire.app.ui.visitor.SearchViewModel

object Routes {
    const val VisitorGraph = "visitor"
    const val Home = "home"
    const val Results = "results"
    const val Detail = "detail/{id}"
    fun detail(id: Long) = "detail/$id"

    const val Help = "help"
    const val Account = "account"
    const val Login = "login"
    const val Register = "register"

    const val ProviderHome = "provider"
    const val ProviderProfile = "provider_profile"
    const val ProviderCert = "provider_cert"
}

@Composable
fun AnnuaireNavHost() {
    val navController = rememberNavController()
    val app = LocalContext.current.annuaireApp
    val scope = rememberCoroutineScope()

    fun onTab(tab: MainTab) {
        when (tab) {
            MainTab.Home -> navController.navigate(Routes.Home) {
                popUpTo(Routes.VisitorGraph) { inclusive = false }
                launchSingleTop = true
            }
            MainTab.Help -> navController.navigate(Routes.Help) { launchSingleTop = true }
            MainTab.Account -> navController.navigate(Routes.Account) { launchSingleTop = true }
        }
    }

    NavHost(navController = navController, startDestination = Routes.VisitorGraph) {
        navigation(startDestination = Routes.Home, route = Routes.VisitorGraph) {
            composable(Routes.Home) {
                val parent = remember(navController) {
                    navController.getBackStackEntry(Routes.VisitorGraph)
                }
                val vm: SearchViewModel = viewModel(
                    viewModelStoreOwner = parent,
                    factory = SearchViewModel.factory(app.repository)
                )
                HomeSearchScreen(
                    onOpenDetail = { id -> navController.navigate(Routes.detail(id)) },
                    onTab = ::onTab,
                    vm = vm
                )
            }
            composable(Routes.Results) {
                val parent = remember(navController) {
                    navController.getBackStackEntry(Routes.VisitorGraph)
                }
                val vm: SearchViewModel = viewModel(
                    viewModelStoreOwner = parent,
                    factory = SearchViewModel.factory(app.repository)
                )
                ResultsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDetail = { id -> navController.navigate(Routes.detail(id)) },
                    vm = vm
                )
            }
            composable(
                Routes.Detail,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                DetailScreen(prestataireId = id, onBack = { navController.popBackStack() })
            }
        }

        composable(Routes.Help) {
            HelpScreen(onTab = ::onTab)
        }

        composable(Routes.Account) {
            AccountHubScreen(
                onContinueVisitor = {
                    navController.popBackStack(Routes.Home, inclusive = false)
                },
                onLogin = { navController.navigate(Routes.Login) },
                onRegister = { navController.navigate(Routes.Register) },
                onOpenProvider = { navController.navigate(Routes.ProviderHome) },
                onLogout = { scope.launch { app.sessionStore.clear() } },
                onTab = ::onTab
            )
        }

        composable(Routes.Login) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoggedIn = { role ->
                    if (role == UserRole.PROVIDER.name) {
                        navController.navigate(Routes.ProviderHome) {
                            popUpTo(Routes.Account)
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Routes.Register) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegistered = {
                    navController.navigate(Routes.ProviderHome) {
                        popUpTo(Routes.Account)
                    }
                }
            )
        }

        composable(Routes.ProviderHome) {
            ProviderHomeScreen(
                onEditProfile = { navController.navigate(Routes.ProviderProfile) },
                onCertification = { navController.navigate(Routes.ProviderCert) },
                onHelp = { navController.navigate(Routes.Help) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ProviderProfile) {
            ProviderProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ProviderCert) {
            CertificationScreen(onBack = { navController.popBackStack() })
        }
    }
}

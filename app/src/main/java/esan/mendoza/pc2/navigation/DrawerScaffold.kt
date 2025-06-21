package esan.mendoza.pc2.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import esan.mendoza.pc2.data.remote.FirebaseAuthManager
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScaffold(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader()
                DrawerBody(
                    navController = navController,
                    drawerState = drawerState,
                    currentRoute = currentRoute,
                    scope = scope
                )
            }
        }
    ) {
        MainContent(
            drawerState = drawerState,
            scope = scope,
            content = content
        )
    }
}

@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Conversor de Monedas",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
    HorizontalDivider()
}

@Composable
private fun DrawerBody(
    navController: NavController,
    drawerState: DrawerState,
    currentRoute: String?,
    scope: CoroutineScope
) {
    val menuItems = listOf(
        MenuItem(
            title = "Inicio",
            icon = Icons.Filled.Home,
            route = "home"
        ),
        MenuItem(
            title = "Convertidor",
            icon = Icons.Filled.Build,
            route = "conversion"
        )
    )

    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        menuItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    scope.launch {
                        navController.navigate(item.route)
                        drawerState.close()
                    }
                },
                icon = { Icon(item.icon, contentDescription = null) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text("Cerrar sesión") },
            selected = false,
            onClick = {
                scope.launch {
                    FirebaseAuthManager.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                    drawerState.close()
                }
            },
            icon = { Icon(Icons.Filled.Close, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onErrorContainer,
                unselectedTextColor = MaterialTheme.colorScheme.onErrorContainer
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    drawerState: DrawerState,
    scope: CoroutineScope,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversor de Monedas") },
                navigationIcon = {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } }
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Abrir menú"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            content()
        }
    }
}

private data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)
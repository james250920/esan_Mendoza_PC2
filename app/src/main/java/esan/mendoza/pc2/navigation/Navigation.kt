package esan.mendoza.pc2.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import esan.mendoza.pc2.presentation.auth.LoginScreen
import esan.mendoza.pc2.presentation.home.HomeScreen


@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login"){
        composable("login"){
            LoginScreen(navController)
        }

        //Navigation
        composable("home"){
            DrawerScaffold(navController) {
                HomeScreen(navController)
            }
        }


    }
}
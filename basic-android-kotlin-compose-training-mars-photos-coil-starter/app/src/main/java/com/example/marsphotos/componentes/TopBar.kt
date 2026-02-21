package com.example.marsphotos.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.marsphotos.model.getMenuItems
import com.example.marsphotos.ui.navigation.currentRoute
import com.example.marsphotos.ui.navigation.getNombreRuta
import kotlinx.coroutines.launch

//opcion experimental para CenterAlignedTopAppBar
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun TopBar(navController: NavController, drawerState: DrawerState){
    // Lanzar corrutina para abrir el menu sin bloquear la UI.
    val scope = rememberCoroutineScope()

    //Tomar la ruta actual y su nombre
    val current = currentRoute(navController)
    val title = getNombreRuta(current)

    CenterAlignedTopAppBar(
        title = { Text(text = title)},
        navigationIcon = {
            IconButton(onClick= {
                //abrir menu lateral
                scope.launch{
                    drawerState.open()
                }
            }) {
                Icon(Icons.Outlined.Menu, "Abrir menu lateral")
            }
        }
    )
}
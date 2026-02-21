package com.example.marsphotos.componentes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.marsphotos.model.getMenuItems
import com.example.marsphotos.ui.navigation.currentRoute

@Composable
fun MenuLateral(
    navController: NavHostController,
    drawerState: DrawerState,
    contenido: @Composable () -> Unit
){
    val menuItems = getMenuItems()

    ModalNavigationDrawer(
        drawerState= drawerState,
        drawerContent = {
            ModalDrawerSheet {
                menuItems.forEach() {item->
                    NavigationDrawerItem(
                        modifier= Modifier.padding(10.dp),
                        icon={
                            Icon( painter = item.icon,
                            contentDescription = item.title )
                        },
                        label = { Text(text = item.title) },
                        selected = currentRoute(navController) == item.ruta,
                        onClick = {
                            navController.navigate(item.ruta)
                        }
                    )
                }
            }
        }
    ) {
        contenido()
    }
}
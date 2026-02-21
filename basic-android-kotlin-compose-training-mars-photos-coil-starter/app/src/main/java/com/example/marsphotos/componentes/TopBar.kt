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
import kotlinx.coroutines.launch

//opcion experimental para CenterAlignedTopAppBar
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun TopBar(drawerState: DrawerState){
    val scope = rememberCoroutineScope()
    CenterAlignedTopAppBar(
        title = { Text(text = "!NOT-ITSUR!") },
        navigationIcon = {
            IconButton(onClick= {
                scope.launch{
                    drawerState.open()
                }
            }) {
                Icon(Icons.Outlined.Menu, "Abrir menu lateral")
            }
        }

    )
}
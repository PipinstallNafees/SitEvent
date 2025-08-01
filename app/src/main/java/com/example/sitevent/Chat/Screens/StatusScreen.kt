package com.example.sitevent.Chat.Screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sitevent.Chat.LCViewModel

@Composable
fun StatusScreen(navController: NavController, vm: LCViewModel){


    Column (modifier = Modifier.padding(16.dp)){
        BottomNavigationMenu(selectedItem = BottomNavigationItem.STATUSLIST,navController)
    }


}
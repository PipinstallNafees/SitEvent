package com.example.sitevent.ui.screen



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.unit.dp

@Composable
fun ChatScreen(
    navController: NavController
){
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<String>() }


    BottomBarScaffold(navController) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxSize(),
                reverseLayout = true
            ) {
                items(messages.reversed()) { msg ->
                    Text(msg, modifier = Modifier.padding(8.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            messages.add(message)
                            message = ""
                        }
                    }
                ) {
                    Text("Send")
                }

            }
        }
    }
}
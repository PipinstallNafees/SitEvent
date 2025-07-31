package com.example.sitevent.ui.screen.Auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sitevent.data.Resource
import com.example.sitevent.ui.Navigation.NavigationItem
import com.example.sitevent.ui.viewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val sendEmailLinkStatus by authViewModel.sendEmailLinkState.collectAsState()
    val verifyState by authViewModel.verifyState.collectAsState()

    LaunchedEffect(verifyState) {
        if (verifyState is Resource.Error) {
            snackbarHostState.showSnackbar(
                (verifyState as Resource.Error).exception.localizedMessage
                    ?: "Verification failed"
            )
        }
        else if(verifyState is Resource.Loading){
            snackbarHostState.showSnackbar("Verifying...")
        }
        else if (verifyState is Resource.Success) {
            navController.navigate(NavigationItem.Home.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Timer state and trigger
    var timeLeft by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    var timerTrigger by remember { mutableStateOf(0) }

    // Start/restart countdown when screen loads or timerTrigger changes
    LaunchedEffect(timerTrigger) {
        canResend = false
        timeLeft = 60
        while (timeLeft > 0) {
            kotlinx.coroutines.delay(1000L)
            timeLeft--
        }
        canResend = true
    }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primary
                    )
                )
            ),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Verify Your Email",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "A verification link was sent to your inbox.\nPlease click it, then return here and tap Continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Continue button
            FilledTonalButton(
                onClick = { authViewModel.checkEmailVerified() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("I’ve Verified – Continue", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(16.dp))

            // Resend section
            if (!canResend) {
                Text(
                    text = "Resend available in $timeLeft s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                TextButton(
                    onClick = {
                        authViewModel.resendVerification()
                        timerTrigger++  // restart countdown
                    }
                ) {
                    Text("Resend Verification Link")
                }
            }

            // Feedback for resend action
            when (sendEmailLinkStatus) {
                Resource.Loading -> {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
                is Resource.Error -> LaunchedEffect(sendEmailLinkStatus) {
                    snackbarHostState.showSnackbar(
                        (sendEmailLinkStatus as Resource.Error).exception.localizedMessage
                            ?: "Failed to resend link"
                    )
                }
                is Resource.Success -> LaunchedEffect(sendEmailLinkStatus) {
                    snackbarHostState.showSnackbar("Verification email resent")
                }
                else -> Unit
            }
        }
    }
}

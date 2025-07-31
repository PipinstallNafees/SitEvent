package com.example.sitevent.ui.screen.Auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.User
import com.example.sitevent.ui.Navigation.NavigationItem
import com.example.sitevent.ui.viewModel.AuthViewModel
import com.example.sitevent.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var sic by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var showConfirmPass by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    val signUpState by authViewModel.signUpState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(signUpState) {
        if (signUpState is Resource.Error) {
            snackbar.showSnackbar(
                (signUpState as Resource.Error).exception.localizedMessage
                    ?: "Sign-up failed"
            )
        }else if (signUpState is Resource.Success) {
            navController.navigate(NavigationItem.Verify.route) {
                popUpTo(NavigationItem.SignUp.route) { inclusive = true }
                launchSingleTop = true
            }
        }
        else if(signUpState is Resource.Loading){
            snackbar.showSnackbar("Signing up...")
        }
    }

    fun passwordStrength(pw: String): Float {
        var score = 0
        if (pw.length >= 8) score++
        if (pw.any { it.isUpperCase() }) score++
        if (pw.any { it.isDigit() }) score++
        if (pw.any { !it.isLetterOrDigit() }) score++
        return (score / 4f).coerceIn(0f, 1f)
    }

    val strength = passwordStrength(password)
    val strengthLabel = when {
        strength < 0.25f -> "Very weak"
        strength < 0.5f -> "Weak"
        strength < 0.75f -> "Medium"
        else -> "Strong"
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
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            // Profile Image Picker
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Select Profile Image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            val fieldModifier = Modifier.fillMaxWidth()
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = fieldModifier,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = fieldModifier,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone (optional)") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = fieldModifier,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = sic,
                onValueChange = { sic = it },
                label = { Text("College SIC") },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                singleLine = true,
                modifier = fieldModifier,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = fieldModifier,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = strength,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = when {
                    strength < 0.5f -> Color.Red
                    strength < 0.75f -> Color.Yellow
                    else -> Color.Green
                }
            )
            Text(
                strengthLabel,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(12.dp))
            val mismatch = confirmPassword.isNotEmpty() && confirmPassword != password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPass = !showConfirmPass }) {
                        Icon(
                            if (showConfirmPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showConfirmPass) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                isError = mismatch,
                modifier = fieldModifier,
                shape = RoundedCornerShape(12.dp)
            )
            if (mismatch) Text(
                "Passwords do not match", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(24.dp))
            FilledTonalButton(
                onClick = {
                    val newUser = User(
                        userId = email,
                        name = name,
                        email = email,
                        sic = sic,
                        phone = phone.ifEmpty { null },
                        profileImageUrl = imageUri?.toString()
                    )
                    authViewModel.signUp(newUser, password)
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) { Text("Sign Up", style = MaterialTheme.typography.labelLarge) }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    "Already have an account? Log in",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // States

//            when (signUpState) {
//                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
//                is Resource.Error -> LaunchedEffect(signUpState) {
//                    snackbar.showSnackbar(
//                        (signUpState as Resource.Error).exception.localizedMessage
//                            ?: "Sign-up failed"
//                    )
//                }
//
//                is Resource.Success -> {
//                    val newUser = User(
//                        name = name,
//                        email = email,
//                        sic = sic,
//                        phone = phone.ifEmpty { null },
//                        profileImageUrl = imageUri?.toString()
//                    )
//                    userViewModel.saveUser(newUser)
//
//                    navController.navigate(NavigationItem.Verify.route) {
//                        popUpTo(NavigationItem.SignUp.route) { inclusive = true }
//                        launchSingleTop = true
//                    }
//                }
//
//                else -> {}
//            }

        }
    }
}

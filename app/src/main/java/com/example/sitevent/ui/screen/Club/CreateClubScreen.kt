package com.example.sitevent.ui.screen.Club

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Club
import com.example.sitevent.data.model.ClubRole
import com.example.sitevent.data.model.ClubUser
import com.example.sitevent.ui.viewModel.ClubViewModel
import com.example.sitevent.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubScreen(
    categoryId: String,
    userId: String,
    navController: NavController,
    clubViewModel: ClubViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    // State
    var clubName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }

    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUri by remember { mutableStateOf<Uri?>(null) }

    // Image pickers
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        logoUri = uri
    }
    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        bannerUri = uri
    }


    // Operation result
    val operationStatus by clubViewModel.operationStatus.collectAsStateWithLifecycle(null)
    LaunchedEffect(operationStatus) {
        if (operationStatus is Resource.Success) {
            Toast.makeText(context, "Club Created!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text("CREATE NEW CLUB", color = colors.primary, fontSize = 20.sp)
                        Text("Let's bring your community to life", color = colors.onSurfaceVariant, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.primary)
                    }
                },
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = clubName,
                onValueChange = { clubName = it },
                label = { Text("Club Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = colors.primary,
                    unfocusedIndicatorColor = colors.onSurfaceVariant,
                    cursorColor = colors.primary,
                    focusedLabelColor = colors.primary,
                    unfocusedLabelColor = colors.onSurfaceVariant
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = colors.primary,
                    unfocusedIndicatorColor = colors.onSurfaceVariant,
                    cursorColor = colors.primary,
                    focusedLabelColor = colors.primary,
                    unfocusedLabelColor = colors.onSurfaceVariant
                )
            )

            Text("Upload Club Logo")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(1.dp, colors.primary, RoundedCornerShape(8.dp))
                    .clickable { logoPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (logoUri != null) {
                    AsyncImage(
                        model = logoUri,
                        contentDescription = "Club Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(32.dp), tint = colors.onSurfaceVariant)
                        Text("Upload Logo", textAlign = TextAlign.Center, color = colors.onSurfaceVariant)
                    }
                }
            }

            Text("Upload Banner Image")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(1.dp, colors.onSurfaceVariant, RoundedCornerShape(8.dp))
                    .clickable { bannerPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (bannerUri != null) {
                    AsyncImage(
                        model = bannerUri,
                        contentDescription = "Banner Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(32.dp), tint = colors.onSurfaceVariant)
                        Text("Upload Banner", textAlign = TextAlign.Center, color = colors.onSurfaceVariant)
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Public Club", modifier = Modifier.weight(1f))
                Switch(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it },
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(

                    onClick = {
                        val id = System.currentTimeMillis().toString()
                        val clubUser = ClubUser(
                            clubId =  id,
                            categoryId = categoryId,
                            userId = userId,
                           role = ClubRole.ADMIN
                        )
                        val club = Club(
                            clubId = id,
                            name = clubName,
                            categoryId = categoryId,
                            description = description,
                            logoUrl = logoUri?.toString(),
                            bannerUrl = bannerUri?.toString(),
                            isPublic = isPublic,
                        )
                        clubViewModel.createClub(club,clubUser)
                    },
                    enabled = clubName.isNotBlank() && operationStatus !is Resource.Loading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create")
                }
            }
        }
    }
}

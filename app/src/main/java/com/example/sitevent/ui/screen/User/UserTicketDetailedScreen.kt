package com.example.sitevent.ui.screen.User

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sitevent.data.Resource
import com.example.sitevent.ui.viewModel.TicketViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTicketDetailedScreen(
    userId: String,
    ticketId: String,
    navController: NavController,
    ticketViewModel: TicketViewModel = hiltViewModel()
) {
    val ticket by ticketViewModel.singleUserTicket.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        ticketViewModel.getSingleUserTicket(userId, ticketId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ticket Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            ticket?.let { it1 ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = ticket!!.eventId.ifEmpty { "Event Name" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Generate QR code from ticket details
                    val qrData = "TicketID: ${ticket!!.ticketId}, EventID: ${ticket!!.eventId}, Category: ${ticket!!.categoryId}"
                    val qrBitmap = generateQrCodeBitmap(qrData)
                    qrBitmap?.let {
                        androidx.compose.foundation.Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }
                    InfoRow("Ticket ID", ticket!!.ticketId)
                    InfoRow("Category", ticket!!.categoryId)
                    InfoRow("Issued At", formatTimestamp(ticket!!.issuedAt))
                    InfoRow("Status", ticket!!.status.name)
                    ticket!!.redeemedAt?.let {
                        InfoRow("Redeemed At", formatTimestamp(it.seconds * 1000))
                    }
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (ticket!!.isValid) "Share Ticket" else "Invalid Ticket")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

fun formatTimestamp(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timeInMillis))
}

fun generateQrCodeBitmap(data: String, size: Int = 512): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}

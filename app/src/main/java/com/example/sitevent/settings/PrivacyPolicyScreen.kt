package com.example.sitevent.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Last updated: 15 Aug 2025",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle("Overview")
            Paragraph(
                "Sit Event (\"we\", \"us\", \"our\") respects your privacy. This policy explains what data we collect, how we use it, how it’s shared, and the choices you have. By using the app, you agree to this policy."
            )

            SectionTitle("Information we collect")
            Bullet("Account info: name, email, profile details you provide.")
            Bullet("Authentication data: identifiers from sign-in providers (e.g., Google).")
            Bullet("Usage data: app interactions, device info, and diagnostics.")
            Bullet("Content you submit: messages, tickets, images, and related metadata.")

            SectionTitle("How we use information")
            Bullet("Provide and operate app features (accounts, tickets, chat).")
            Bullet("Security, fraud prevention, and abuse detection.")
            Bullet("Improve performance, analytics, and user experience.")
            Bullet("Send important notifications and service updates.")

            SectionTitle("Legal bases (EEA/UK)")
            Bullet("Contract: to provide requested services.")
            Bullet("Legitimate interests: to secure and improve the app.")
            Bullet("Consent: where required (e.g., certain notifications).")

            SectionTitle("Sharing and disclosure")
            Bullet("Service providers (e.g., hosting, analytics, messaging) under contract.")
            Bullet("When required by law or to protect rights, safety, and security.")
            Bullet("With your direction or consent (e.g., sharing content).")

            SectionTitle("Data retention")
            Paragraph(
                "We keep data for as long as needed to provide the service and for legitimate business or legal purposes. We delete or anonymize data when no longer required."
            )

            SectionTitle("Security")
            Paragraph(
                "We use technical and organizational measures to protect your data. No method of transmission or storage is 100% secure; risks may remain."
            )

            SectionTitle("Children’s privacy")
            Paragraph(
                "Our app isn’t directed to children under the age where parental consent is required by law. If we learn we collected such data, we’ll take steps to delete it."
            )

            SectionTitle("Your choices and rights")
            Bullet("Access, update, or delete your account information.")
            Bullet("Control notifications in system settings or app preferences.")
            Bullet("Withdraw consent where processing is based on consent.")
            Paragraph(
                "Depending on your region, you may have additional rights (e.g., data portability, objection). Contact us to exercise these rights."
            )

            SectionTitle("International transfers")
            Paragraph(
                "Your information may be processed in countries other than your own. We take steps to ensure appropriate safeguards are in place."
            )

            SectionTitle("Changes to this policy")
            Paragraph(
                "We may update this policy from time to time. Material changes will be communicated within the app or by other appropriate means."
            )

            SectionTitle("Contact us")
            Paragraph(
                "Questions or requests?\n Contact: support@sitevent.example"
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
    )
}

@Composable
private fun Paragraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun Bullet(text: String) {
    Text(
        text = "• " + text,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

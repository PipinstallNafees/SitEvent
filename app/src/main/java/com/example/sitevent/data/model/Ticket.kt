package com.example.sitevent.data.model

enum class RegistrationStatus { PENDING, CONFIRMED, CANCELLED }

/**
 * Combines the event‐registration intent with the actual ticket credential.
 */
data class Ticket(

    val ticketId: String = System.currentTimeMillis().toString(),
    val categoryId: String = "",
    val clubId: String= "",
    val eventId: String = "",
    val userId: String = "",
    val issuedAt: Long = System.currentTimeMillis(),
    val qrCodeUrl: String? = null,

    val status: RegistrationStatus = RegistrationStatus.CONFIRMED,
    val groupMemberIds: List<String> = emptyList(),

    val redeemedAt: com.google.firebase.Timestamp? = null,
    val isValid: Boolean = true,
)

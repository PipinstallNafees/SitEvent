package com.example.sitevent.data.model

enum class RegistrationStatus { PENDING, CONFIRMED, CANCELLED ,CLAIMED}

/**
 * Combines the event‐registration intent with the actual ticket credential.
 */
data class Ticket(

    val ticketId: String = System.currentTimeMillis().toString(),
    val categoryId: String = "",
    val clubId: String= "",
    val eventId: String = "",
    val userId: String = "",
    val teamId: String  = "",
    val issuedAt: Long = System.currentTimeMillis(),
    val qrCodeUrl: String? = null,
    val participantIds: List<String> = emptyList(),
    val status: RegistrationStatus = RegistrationStatus.CONFIRMED,

    val redeemedAt: com.google.firebase.Timestamp? = null,
    val isValid: Boolean = true,
    val additionalInfoAskByEventOrganizer: List<AdditionalInfoAskFromUser> = emptyList(),
)


data class Team(
    val teamId: String = System.currentTimeMillis().toString(),
    val eventId: String = "",
    val subEventId: String? = null,
    val clubId: String = "",
    val categoryId: String = "",
    val teamName: String = "",
    val teamMemberIds: List<String> = emptyList(),
    val teamLeaderId: String = "",
    val teamScore: Int = 0,
)

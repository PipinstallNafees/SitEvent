package com.example.sitevent.data.model


enum class AppRole { ADMIN, MEMBER }
data class User(
    val userId: String = "",
    val name: String= "",
    val email: String = "",
    val sic: String = "",
    val phone: String? = null,
    val appRole: AppRole = AppRole.MEMBER,
    val profileImageUrl: String? = null,
    val clubs: List<UserClub> = emptyList(),
    val events: List<UserEvent> = emptyList(),
    val tickets: List<UserTicket> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long? = null,
    val isOnline: Boolean = false
)

data class UserClub(
    val clubId: String = "",
    val categoryId: String = ""
)

data class UserEvent(
    val categoryId: String = "",
    val clubId: String = "",
    val eventId: String = "",
)

data class UserTicket(
    val ticketId: String = "",
    val categoryId: String = "",
    val clubId: String = "",
    val eventId: String = ""
)










package com.example.sitevent.data.repository.Inteface

import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Ticket
import kotlinx.coroutines.flow.Flow


interface TicketRepository {

    suspend fun issueTicket(ticket: Ticket): Resource<Unit>

    suspend fun updateTicket(ticket: Ticket): Resource<Unit>

    suspend fun deleteTicket(ticket: Ticket): Resource<Unit>

    suspend fun redeemTicket(categoryId: String, clubId: String, eventId: String, ticketId: String,userId: String): Resource<Unit>


    fun getTicket(categoryId: String, clubId: String, eventId: String, ticketId: String): Flow<Ticket?>
    fun getAllTicketsForEvent(categoryId: String, clubId: String, eventId: String): Flow<List<Ticket>>
    fun getAllTickets(): Flow<List<Ticket>>

    fun getAllTicketsForUser(userId: String): Flow<List<Ticket>>
    fun getSingleTicketForUser(userId: String, ticketId: String): Flow<Ticket?>

}
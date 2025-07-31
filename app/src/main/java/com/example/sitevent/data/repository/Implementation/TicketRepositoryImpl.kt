package com.example.sitevent.data.repository.Implementation

import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Ticket
import com.example.sitevent.data.repository.Inteface.TicketRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TicketRepository {

    private val USER = "Users"
    private val TICKETS = "Tickets"
    private val CATEGORIES = "Categories"
    private val CLUBS = "Clubs"
    private val EVENTS = "Events"


    private val ticketsRoot = firestore.collection("Tickets")
    private fun userCollection() = firestore
        .collection(USER)


    private fun eventCollection(categoryId: String, clubId: String) = firestore
        .collection(CATEGORIES)
        .document(categoryId)
        .collection(CLUBS)
        .document(clubId)
        .collection(EVENTS)


    override suspend fun issueTicket(ticket: Ticket): Resource<Unit> = try {
        //save in user's collection
        userCollection()
            .document(ticket.userId)
            .collection(TICKETS)
            .document(ticket.ticketId)
            .set(ticket)
            .await()

        //save in event's  collection
        eventCollection(ticket.categoryId, ticket.clubId)
            .document(ticket.eventId)
            .collection(TICKETS)
            .document(ticket.ticketId)
            .set(ticket)
            .await()

        //save ticket id in event details
        eventCollection(ticket.categoryId, ticket.clubId)
            .document(ticket.eventId)
            .update("ticketIds", FieldValue.arrayUnion(ticket.ticketId))
            .await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun updateTicket(ticket: Ticket): Resource<Unit> = try {
        //update in user's collection
        userCollection()
            .document(ticket.userId)
            .collection(TICKETS)
            .document(ticket.ticketId)
            .set(ticket)
            .await()

        //update in event's  collection
        eventCollection(ticket.categoryId, ticket.clubId)
            .document(ticket.eventId)
            .collection(TICKETS)
            .document(ticket.ticketId)
            .set(ticket)
            .await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun deleteTicket(ticket: Ticket): Resource<Unit> = try {
        //delete in user's collection
        userCollection()
            .document(ticket.userId)
            .collection(TICKETS)
            .document(ticket.ticketId)
            .delete()
            .await()

        //delete in event's  collection
        eventCollection(ticket.categoryId, ticket.clubId)
            .document(ticket.eventId)
            .collection(TICKETS)
            .document(ticket.ticketId)
            .delete()
            .await()

        //delete from event details
        eventCollection(ticket.categoryId, ticket.clubId)
            .document(ticket.eventId)
            .update("ticketIds", FieldValue.arrayRemove(ticket.ticketId))
            .await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun redeemTicket(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        userId: String
    ): Resource<Unit> = try {

        //update in events ticket
        eventCollection(categoryId, clubId)
            .document(eventId)
            .collection(TICKETS)
            .document(ticketId)
            .update(
                mapOf(
                    "isValid" to false,
                    "redeemedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()

        //update in user ticket
        firestore.collection(USER)
            .document(userId)
            .collection(TICKETS)
            .document(ticketId)
            .update(
                mapOf(
                    "isValid" to false,
                    "redeemedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override fun getTicket(categoryId: String, clubId: String, eventId: String, ticketId: String): Flow<Ticket?> = callbackFlow {
        val registration: ListenerRegistration = eventCollection(categoryId, clubId)
            .document(eventId)
            .collection(TICKETS)
            .document(ticketId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObject(Ticket::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }
    }


    override fun getAllTicketsForEvent(categoryId: String, clubId: String, eventId: String): Flow<List<Ticket>> = callbackFlow {
        val registration: ListenerRegistration = eventCollection(categoryId, clubId)
            .document(eventId)
            .collection(TICKETS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Ticket::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }

    }

    override fun getAllTickets(): Flow<List<Ticket>> = callbackFlow {
        val registration: ListenerRegistration = ticketsRoot
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Ticket::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }
    }

    override fun getAllTicketsForUser(userId: String): Flow<List<Ticket>> = callbackFlow {

        val registration: ListenerRegistration = userCollection()
            .document(userId)
            .collection(TICKETS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Ticket::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }

    }

    override fun getSingleTicketForUser(userId: String, ticketId: String): Flow<Ticket?> = callbackFlow {
        val registration: ListenerRegistration = userCollection()
            .document(userId)
            .collection(TICKETS)
            .document(ticketId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObject(Ticket::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }
    }
}

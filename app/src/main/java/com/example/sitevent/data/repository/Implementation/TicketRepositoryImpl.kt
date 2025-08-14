package com.example.sitevent.data.repository.Implementation

import android.util.Log
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.RegistrationStatus
import com.example.sitevent.data.model.Team
import com.example.sitevent.data.model.Ticket
import com.example.sitevent.data.model.User
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
    private val TEAMS = "Teams"


    private val ticketsRoot = firestore.collection("Tickets")
    private fun userCollection() = firestore
        .collection(USER)


    private fun eventCollection(categoryId: String, clubId: String) = firestore
        .collection(CATEGORIES)
        .document(categoryId)
        .collection(CLUBS)
        .document(clubId)
        .collection(EVENTS)


    override suspend fun issueTicket(ticket: Ticket,team: Team): Resource<Unit> = try {

        val batch = firestore.batch()

        val ticketRef = firestore
            .collection(TICKETS)
            .document(ticket.ticketId)


        //save team collection
        val teamRef = firestore
            .collection(TICKETS)
            .document(ticket.ticketId)
            .collection(TEAMS)
            .document(team.teamId)

        batch.set(ticketRef, ticket)
        batch.set(teamRef, team)

        batch.commit().await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun updateTicket(ticket: Ticket,team: Team): Resource<Unit> = try {

        val batch = firestore.batch()
        //update in team collection
        val teamRef = firestore
            .collection(TICKETS)
            .document(ticket.ticketId)
            .collection(TEAMS)
            .document(team.teamId)


        val ticketRef = firestore
            .collection(TICKETS)
            .document(ticket.ticketId)

        batch.set(teamRef, team)
        batch.set(ticketRef, ticket)

        batch.commit().await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun cancelTicket(ticketId: String): Resource<Unit> = try {


        Log.d("TicketRepository", "Cancelling ticket with ID: $ticketId")
        val batch = firestore.batch()
      val ticketRef = firestore
            .collection(TICKETS)
            .document(ticketId)


        val teamRef = firestore
            .collection(TICKETS)
            .document(ticketId)
            .collection(TEAMS)
            .document(ticketId)

        batch.delete(ticketRef)
        batch.delete(teamRef)
        batch.commit().await()

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

       firestore
            .collection(TICKETS)
            .document(ticketId)
            .update(
                mapOf(
                    "isValid" to false,
                    "redeemedAt" to FieldValue.serverTimestamp(),
                    "status" to RegistrationStatus.CLAIMED
                )
            )
            .await()


        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override fun getTicket(categoryId: String, clubId: String, eventId: String, ticketId: String): Flow<Ticket?> = callbackFlow {
        val registration: ListenerRegistration = firestore
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
        val registration: ListenerRegistration = firestore
            .collection(TICKETS)
            .whereEqualTo("eventId",eventId)
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
        val registration: ListenerRegistration = firestore.collection(TICKETS)
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

        val registration: ListenerRegistration = firestore
            .collection(TICKETS)
            .whereEqualTo("userId",userId)
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
        val registration: ListenerRegistration = firestore
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

    override fun getTeamForTicket(
        ticketId: String,
        teamId: String
    ): Flow<Team?> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(TICKETS)
            .document(ticketId)
            .collection(TEAMS)
            .document(teamId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObject(Team::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }

    }

    override fun getAllTeamsForEvent(
        categoryId: String,
        clubId: String,
        eventId: String
    ): Flow<List<Team>> = callbackFlow{
        val registration: ListenerRegistration = firestore
            .collectionGroup(TEAMS)
            .whereEqualTo("eventId",eventId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Team::class.java)).isSuccess }
            }
        awaitClose { registration.remove() }
    }

    override fun getAllMembersNotRegistered(
        categoryId: String,
        clubId:     String,
        eventId:    String,
        participantIds: List<String>
    ): Flow<List<User>> = callbackFlow {
        val query = when {
            participantIds.isEmpty() -> {
                // no filter → get everyone
                firestore.collection(USER)
            }
            participantIds.size <= 10 -> {
                // safe to use whereNotIn
                firestore.collection(USER)
                    .whereNotIn("userId", participantIds)
            }
            else -> {
                // too many IDs for whereNotIn: fetch all, filter in code below
                firestore.collection(USER)
            }
        }

        val registration = query.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            snap?.let {
                val all = it.toObjects(User::class.java)
                val filtered = if (participantIds.size <= 10) {
                    // query already excluded them
                    all
                } else {
                    // manually filter out the large list
                    all.filter { user -> user.userId !in participantIds }
                }
                trySend(filtered).isSuccess
            }
        }

        awaitClose { registration.remove() }
    }

    override fun getTicketForUserInEvent(eventId: String, userId: String): Flow<Ticket?> = callbackFlow {
        val registration: ListenerRegistration = firestore
            .collection(TICKETS)
            .whereEqualTo("userId",userId)
            .whereEqualTo("eventId",eventId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Ticket::class.java).firstOrNull()).isSuccess }
            }
        awaitClose { registration.remove() }
    }



}

package com.example.sitevent.data.repository.Implementation

import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Event
import com.example.sitevent.data.repository.Inteface.EventRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
) : EventRepository {

    private val EVENTS = "Events"
    private val CLUBS = "Clubs"
    private val categoryCol = firebaseFirestore.collection("Categories")

    private fun eventsCollection(categoryId: String, clubId: String) =
        categoryCol
            .document(categoryId)
            .collection(CLUBS)
            .document(clubId)
            .collection(EVENTS)


    override suspend fun createEvent(event: Event): Resource<Unit> = try {
        eventsCollection(event.categoryId, event.clubId)
            .document(event.eventId)
            .set(event)
            .await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    // ← now takes all three IDs and reads directly by path

    override suspend fun updateEvent(event: Event): Resource<Unit> = try {
        eventsCollection(event.categoryId, event.clubId)
            .document(event.eventId)
            .set(event)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun deleteEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
    ): Resource<Unit> = try {
        eventsCollection(categoryId, clubId)
            .document(eventId)
            .delete()
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override fun getEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
    ): Flow<Event?> = callbackFlow {
        val registration = eventsCollection(categoryId, clubId)
            .document(eventId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err); return@addSnapshotListener
                }
                snap?.let { trySend(it.toObject(Event::class.java)) }
            }
        awaitClose { registration.remove() }
    }


    override fun getAllEvents(): Flow<List<Event>> = callbackFlow {
        val registration: ListenerRegistration = firebaseFirestore
            .collectionGroup(EVENTS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err); return@addSnapshotListener
                }
                snap?.let { trySend(it.toObjects(Event::class.java)) }
            }
        awaitClose { registration.remove() }
    }

    override fun getEventsByClubId(categoryId: String, clubId: String): Flow<List<Event>> =
        callbackFlow {
            val registration: ListenerRegistration = eventsCollection(categoryId, clubId)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        close(err); return@addSnapshotListener
                    }
                    snap?.let { trySend(it.toObjects(Event::class.java)) }
                }
            awaitClose { registration.remove() }
        }

    override suspend fun saveTicketInEvent(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String
    ): Resource<Unit> {
        return try {
            eventsCollection(categoryId, clubId)
                .document(eventId)
                .update("ticketIds", FieldValue.arrayUnion(ticketId))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

}

package com.example.sitevent.data.repository.Implementation

import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Club
import com.example.sitevent.data.model.ClubUser
import com.example.sitevent.data.repository.Inteface.ClubRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ClubRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
) : ClubRepository {

    private val categoryCollection = firebaseFirestore.collection("Categories")
    private val CATEGORIES = "Categories"
    private val CLUBS = "Clubs"
    private val MEMBERS = "Members"


    private fun clubsCollection(categoryId: String) =
        categoryCollection
            .document(categoryId)
            .collection(CLUBS)


    override suspend fun createClub(club: Club, clubUsr: ClubUser): Resource<Unit> = try {

        val batch = firebaseFirestore.batch()

        val clubUsrRef = firebaseFirestore
            .collection(CATEGORIES)
            .document(club.categoryId)
            .collection(CLUBS)
            .document(club.clubId)
            .collection(MEMBERS)
            .document(clubUsr.userId)

        val clubRef = firebaseFirestore.collection(CATEGORIES)
            .document(club.categoryId)
            .collection(CLUBS)
            .document(club.clubId)

        batch.set(clubUsrRef, clubUsr)
        batch.set(clubRef, club)
        batch.commit().await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }


    override suspend fun updateClub(club: Club): Resource<Unit> = try {
        firebaseFirestore.collection(CATEGORIES)
            .document(club.categoryId)
            .collection(CLUBS)
            .document(club.clubId)
            .set(club)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun deleteClub(categoryId: String, clubId: String): Resource<Unit> = try {
        firebaseFirestore.collection(CATEGORIES)
            .document(categoryId).collection(CLUBS)
            .document(clubId)
            .delete()
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }


    override fun getClub(categoryId: String, clubId: String): Flow<Club?> = callbackFlow {
        val registration: ListenerRegistration = firebaseFirestore
            .collection(CATEGORIES)
            .document(categoryId)
            .collection(CLUBS)
            .document(clubId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Club::class.java)).isSuccess
            }
        awaitClose { registration.remove() }
    }

    override fun getAllClubsByCategory(categoryId: String): Flow<List<Club>> = callbackFlow {
        val registration: ListenerRegistration = firebaseFirestore
            .collection(CATEGORIES)
            .document(categoryId)
            .collection(CLUBS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Club::class.java)).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }

    override fun getAllClubs(): Flow<List<Club>> = callbackFlow {
        val registration: ListenerRegistration = firebaseFirestore
            .collectionGroup(CLUBS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Club::class.java)).isSuccess
                }
            }
        awaitClose { registration.remove() }
    }


    override suspend fun joinClub(
        categoryId: String,
        clubId: String,
        userId: String
    ): Resource<Unit> = try {
        val batch = firebaseFirestore.batch()

        val clubUser = ClubUser(userId, clubId, categoryId)

        val memberRef = firebaseFirestore
            .collection(CATEGORIES)
            .document(categoryId)
            .collection(CLUBS)
            .document(clubId)
            .collection(MEMBERS)
            .document(userId)

        val clubRef = firebaseFirestore.collection(CATEGORIES)
            .document(categoryId)
            .collection(CLUBS)
            .document(clubId)


        batch.set(memberRef, clubUser)
        batch.update(clubRef, "members", FieldValue.arrayUnion(userId))
        batch.commit().await()


        Resource.Success(Unit)
    } catch(e: Exception) {
        Resource.Error(e)
    }


    override suspend fun leaveClub(
        categoryId: String,
        clubId: String,
        userId: String,
    ): Resource<Unit> = try {
        val batch = firebaseFirestore.batch()

        val memberRef = firebaseFirestore
            .collection(CATEGORIES)
            .document(categoryId)
            .collection(CLUBS)
            .document(clubId)
            .collection(MEMBERS)
            .document(userId)

        val clubRef = firebaseFirestore
            .collection(CATEGORIES)
            .document(categoryId)
            .collection(CLUBS)
            .document(clubId)

        batch.delete(memberRef)
        batch.update(clubRef, "members", FieldValue.arrayRemove(userId))
        batch.commit().await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun changeClubVisibility(
        categoryId: String,
        clubId: String,
        visibility: Boolean,
    ): Resource<Unit> {
        return try {
            firebaseFirestore
                .collection(CATEGORIES)
                .document(categoryId)
                .collection(CLUBS)
                .document(clubId)
                .update("isPublic", visibility)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun addEventToClub(
        categoryId: String,
        clubId: String,
        eventId: String,
    ): Resource<Unit> {
        return try {
            firebaseFirestore
                .collection(CATEGORIES)
                .document(categoryId)
                .collection(CLUBS)
                .document(clubId)
                .update("events", FieldValue.arrayUnion(eventId))
                .await()
            Resource.Success(Unit)
        }
        catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun removeEventFromClub(
        categoryId: String,
        clubId: String,
        eventId: String,
    ): Resource<Unit>{
        return try {
            firebaseFirestore
                .collection(CATEGORIES)
                .document(categoryId)
                .collection(CLUBS)
                .document(clubId)
                .update("events", FieldValue.arrayRemove(eventId))
                .await()
            Resource.Success(Unit)
        }
        catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override fun getAllClubMembers(categoryId: String, clubId: String): Flow<List<ClubUser>> =
        callbackFlow {
            val registration: ListenerRegistration = firebaseFirestore
                .collection(CATEGORIES)
                .document(categoryId)
                .collection(CLUBS)
                .document(clubId)
                .collection(MEMBERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        trySend(snapshot.toObjects(ClubUser::class.java)).isSuccess
                    }
                }
            awaitClose { registration.remove() }
        }

    override fun getClubMember(
        categoryId: String,
        clubId: String,
        userId: String,
    ): Flow<ClubUser?> = callbackFlow {
        val registration: ListenerRegistration = firebaseFirestore
            .collection(CATEGORIES)
            .document(categoryId)
            .collection(CLUBS)
            .document(clubId)
            .collection(MEMBERS)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(ClubUser::class.java)).isSuccess
            }
        awaitClose { registration.remove() }
    }


    override suspend fun changeClubUserRole(
        categoryId: String,
        clubId: String,
        userId: String,
        role: String,
    ): Resource<Unit> {
        val docRef = firebaseFirestore
            .collection(CATEGORIES)
            .document(categoryId)
            .collection(CLUBS)
            .document(clubId)
            .collection(MEMBERS)
            .document(userId)
            .update("role", role)
            .addOnSuccessListener {
                Resource.Success(Unit)
            }
            .addOnFailureListener {
                Resource.Error(it)
            }
        return Resource.Success(Unit)
    }

}

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


    override suspend fun createClub(club: Club): Resource<Unit> = try {
        clubsCollection(club.categoryId)
            .document(club.clubId)
            .set(club)
            .await()

        firebaseFirestore.collection(CATEGORIES)
            .document(club.categoryId)
            .update("clubs", FieldValue.arrayUnion(club.clubId))
            .await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }


    override suspend fun updateClub(club: Club): Resource<Unit> = try {
        clubsCollection(club.categoryId)
            .document(club.clubId)
            .set(club)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun deleteClub(categoryId: String, clubId: String): Resource<Unit> = try {
        clubsCollection(categoryId)
            .document(clubId)
            .delete()
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }


    override fun getClub(categoryId: String, clubId: String): Flow<Club?> = callbackFlow {
        val registration: ListenerRegistration = clubsCollection(categoryId)
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
        val registration: ListenerRegistration = clubsCollection(categoryId)
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
        userId: String,
    ): Resource<Unit> = try {
        // 1) write member doc under Members

        val clubUser = ClubUser(userId = userId, clubId = clubId, categoryId = categoryId)

        clubsCollection(categoryId)
            .document(clubId)
            .collection(MEMBERS)
            .document(userId)
            .set(clubUser)
            .await()

        // 2) append to Club.members array
        clubsCollection(categoryId)
            .document(clubId)
            .update("members", FieldValue.arrayUnion(userId))
            .await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun leaveClub(
        userId: String,
        categoryId: String,
        clubId: String,
    ): Resource<Unit> = try {
        // 1) delete member doc
        clubsCollection(categoryId)
            .document(clubId)
            .collection(MEMBERS)
            .document(userId)
            .delete()
            .await()

        // 2) remove from Club.members array
        clubsCollection(categoryId)
            .document(clubId)
            .update("members", FieldValue.arrayRemove(userId))
            .await()

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
            clubsCollection(categoryId)
                .document(clubId)
                .update("isPublic", visibility)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }



    override fun getAllClubMembers(categoryId: String, clubId: String): Flow<List<ClubUser>> =
        callbackFlow {
            val registration: ListenerRegistration = clubsCollection(categoryId)
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
        val registration: ListenerRegistration = clubsCollection(categoryId)
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
        val docRef = clubsCollection(categoryId)
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

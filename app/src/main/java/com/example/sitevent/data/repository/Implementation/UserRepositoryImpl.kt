package com.example.sitevent.data.repository.Implementation

import com.example.sitevent.data.model.User
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.UserClub
import com.example.sitevent.data.model.UserTicket
import com.example.sitevent.data.repository.Inteface.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    private val userCol = firebaseFirestore.collection("Users")

    // One-time suspend operations
    override suspend fun saveUser(user: User): Resource<Unit> = try {
        userCol.document(user.email).set(user).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getUser(userId: String): Resource<User?> = try {
        val user = userCol.document(userId).get().await().toObject(User::class.java)
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun updateUser(user: User): Resource<Unit> = try {
        userCol.document(user.email).set(user).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun deleteUser(userId: String): Resource<Unit> = try {
        userCol.document(userId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getAllUsers(): Resource<List<User>> = try {
        val users = userCol.get().await().toObjects(User::class.java)
        Resource.Success(users)
    } catch (e: Exception) {
        Resource.Error(e)
    }

    override suspend fun getCurrentUser(): User? = try {
        val email = firebaseAuth.currentUser?.email ?: return null
        firebaseFirestore.collection("Users").document(email).get().await().toObject(User::class.java)
    } catch (_: Exception) {
        null
    }

    override suspend fun joinClubForUser(userId: String, categoryId: String, clubId: String): Resource<Unit> {
        return try {

            val userClub = UserClub(
                clubId = clubId,
                categoryId = categoryId,
            )
            userCol.document(userId)
                .collection("UserClubs")
                .document(clubId)
                .set(userClub)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
    override suspend fun leaveClubForUser(userId: String, categoryId: String, clubId: String): Resource<Unit> {
        return try {

            userCol.document(userId)
                .collection("UserClubs")
                .document(clubId)
                .delete()
                .await()

            Resource.Success(Unit)
            } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun issueTicketForUser(userId: String, categoryId: String, clubId: String, eventId: String,ticketId:String): Resource<Unit> {
        return try {
            val userRef = userCol.document(userId)

            userRef.update("tickets", FieldValue.arrayUnion(ticketId)).await()
            Resource.Success(Unit)

        }
        catch (e: Exception) {
            Resource.Error(e)
        }
    }



    // Real-time streams
    override fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val registration: ListenerRegistration = userCol.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            snap?.let { trySend(it.toObjects(User::class.java)).isSuccess }
        }
        awaitClose { registration.remove() }
    }

    override fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val registration: ListenerRegistration = userCol.document(userId)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val user = snap?.toObject(User::class.java)
                trySend(user).isSuccess
            }
        awaitClose { registration.remove() }
    }

    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val currentEmail = firebaseAuth.currentUser?.email
        if (currentEmail == null) {
            trySend(null).isSuccess
            close()
        } else {
            val registration: ListenerRegistration = userCol.document(currentEmail)
                .addSnapshotListener { snap, err ->
                    if (err != null) { close(err); return@addSnapshotListener }
                    val user = snap?.toObject(User::class.java)
                    trySend(user).isSuccess
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun cancelTicketForUser(userId: String, ticketId: String): Resource<Unit> {
        return try {
            firebaseFirestore
                .collection("Users")
                .document(userId)
                .update("tickets", FieldValue.arrayRemove(ticketId))
                .await()
            Resource.Success(Unit)
        } catch (e:Exception) {
            Resource.Error(e)
        }

    }


}
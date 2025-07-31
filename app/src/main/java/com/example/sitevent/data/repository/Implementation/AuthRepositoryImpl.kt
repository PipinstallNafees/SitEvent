package com.example.sitevent.data.repository.Implementation

// Add these imports
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.User
import com.example.sitevent.data.repository.Inteface.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository
{

    override suspend fun login(email: String, password: String): Resource<Unit> {
        return try {
            val result = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()
            val user = result.user
            if (user != null && !user.isEmailVerified) {
                firebaseAuth.signOut()
                Resource.Error(Exception("Email not verified, check your inbox"))
            } else {
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }


    val collection = firestore.collection("Users")

    override suspend fun signUp(user: User, password: String): Resource<Unit> {
        return try {
            firebaseAuth
                .createUserWithEmailAndPassword(user.email, password)
                .await()

            collection.document(user.email).set(user).await()

            sendVerificationEmail()

        }  catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun sendVerificationEmail(): Resource<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                Resource.Success(Unit)
            } else {
                Resource.Error(Exception("User Not Logged In"))
            }

        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun refreshUserEmailVerification(): Resource<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.reload().await()
                if (user.isEmailVerified) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(Exception("Email still not verified"))
                }
            } else {
                Resource.Error(Exception("No logged-in user to refresh"))
            }

        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return  try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        }catch (e: Exception){
            Resource.Error(e)
        }

    }

    override suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override fun signInStatus(): Boolean {
        val user = firebaseAuth.currentUser
        return user != null && user.isEmailVerified
    }
    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}
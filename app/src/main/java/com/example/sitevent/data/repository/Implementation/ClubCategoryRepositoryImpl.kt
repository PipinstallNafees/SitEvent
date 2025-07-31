package com.example.sitevent.data.repository.Implementation

import androidx.compose.animation.core.snap
import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Category
import com.example.sitevent.data.repository.Inteface.ClubCategoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ClubCategoryRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : ClubCategoryRepository {
    private val categoryCollection = firebaseFirestore.collection("Categories")

    override suspend fun saveCategory(category: Category): Resource<Unit> {
        return try {
            categoryCollection.document(category.categoryId)
                .set(category)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun updateCategory(category: Category): Resource<Unit> {
        return try {
            categoryCollection.document(category.categoryId)
                .set(category)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun deleteCategory(categoryId: String): Resource<Unit> {
        return try {
            categoryCollection.document(categoryId)
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override fun getCategory(categoryId: String): Flow<Category?> = callbackFlow {
        val listenerRegistration: ListenerRegistration = categoryCollection
            .document(categoryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val category = snapshot?.toObject(Category::class.java)

                if (category != null) {
                    trySend(category)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override fun getAllCategories(): Flow<List<Category>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = categoryCollection
            .addSnapshotListener { snap, err->
                if(err != null){
                    close(err)
                    return@addSnapshotListener
                }
                val categories = snap?.toObjects(Category::class.java)
                if(categories != null){
                    trySend(categories)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }


}

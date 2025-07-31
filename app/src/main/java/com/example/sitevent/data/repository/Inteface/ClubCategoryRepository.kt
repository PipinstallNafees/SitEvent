package com.example.sitevent.data.repository.Inteface

import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Category
import kotlinx.coroutines.flow.Flow

interface ClubCategoryRepository{
    suspend fun saveCategory(category: Category): Resource<Unit>
    suspend fun updateCategory(category: Category): Resource<Unit>
    suspend fun deleteCategory(categoryId: String): Resource<Unit>

    fun getCategory(categoryId: String): Flow<Category?>
    fun getAllCategories(): Flow<List<Category>>


}
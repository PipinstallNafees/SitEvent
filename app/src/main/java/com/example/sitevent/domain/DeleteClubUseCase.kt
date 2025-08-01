package com.example.sitevent.domain

import com.example.sitevent.data.Resource
import com.example.sitevent.data.repository.Inteface.ClubCategoryRepository
import com.example.sitevent.data.repository.Inteface.ClubRepository
import javax.inject.Inject

class DeleteClubUseCase @Inject constructor(
    private val clubRepository: ClubRepository,
    private val categoryRepository: ClubCategoryRepository
) {
    suspend operator fun invoke(categoryID: String, clubId: String): Resource<Unit>{
        when(val result = clubRepository.deleteClub(categoryID, clubId)){
            is Resource.Error -> return result
            is Resource.Success -> {
                categoryRepository.removeClubFromCategory(categoryID, clubId)
                return result
            }
            else -> {}

        }
        return Resource.Error(Exception("Unknown error"))
    }
}
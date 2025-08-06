package com.example.sitevent.domain

import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Club
import com.example.sitevent.data.model.ClubUser
import com.example.sitevent.data.repository.Inteface.ClubCategoryRepository
import com.example.sitevent.data.repository.Inteface.ClubRepository
import javax.inject.Inject

class CreateClubUseCase @Inject constructor(
    private val clubRepository: ClubRepository,
    private val categoryRepository: ClubCategoryRepository
) {
    suspend operator fun invoke(club: Club,clubUser: ClubUser): Resource<Unit> {
        when(val result = clubRepository.createClub(club,clubUser)){
            is Resource.Error -> return result
            is Resource.Success -> {
                categoryRepository.addClubToCategory(club.categoryId, club.clubId)
                return result
            }
            else -> {}
        }
        return Resource.Error(Exception("Unknown error"))
    }

}
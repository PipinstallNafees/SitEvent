package com.example.sitevent.domain

import com.example.sitevent.data.Resource
import com.example.sitevent.data.repository.Inteface.ClubRepository
import com.example.sitevent.data.repository.Inteface.UserRepository
import javax.inject.Inject

class JoinClubUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val clubRepository: ClubRepository
)
{
    suspend operator fun invoke(categoryId: String, clubId: String,userId: String) : Resource<Unit>{
        when(val result = clubRepository.joinClub(categoryId, clubId, userId)){
            is Resource.Error -> return result
            is Resource.Success -> {
                userRepository.joinClubForUser(userId, categoryId, clubId)
                return result
            }
            else -> {}
        }
        return Resource.Error(Exception("Unknown error"))

    }
}
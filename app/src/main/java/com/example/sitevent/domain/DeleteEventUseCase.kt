package com.example.sitevent.domain

import com.example.sitevent.data.Resource
import com.example.sitevent.data.repository.Inteface.ClubRepository
import com.example.sitevent.data.repository.Inteface.EventRepository
import javax.inject.Inject

class DeleteEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val clubRepository: ClubRepository
) {
    suspend operator fun invoke(categoryId: String, clubId: String, eventId: String): Resource<Unit> {
        when (val result = eventRepository.deleteEvent(categoryId, clubId, eventId)) {
            is Resource.Error -> return result
            is Resource.Success -> {
                clubRepository.removeEventFromClub(categoryId, clubId, eventId)
                return result
            }
            else -> {}
        }
        return Resource.Error(Exception("Unknown error"))

    }
}

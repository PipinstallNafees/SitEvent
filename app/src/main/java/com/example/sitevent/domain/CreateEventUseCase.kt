package com.example.sitevent.domain

import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Event
import com.example.sitevent.data.repository.Inteface.ClubRepository
import com.example.sitevent.data.repository.Inteface.EventRepository
import javax.inject.Inject

class CreateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val clubRepository: ClubRepository,
) {
    suspend operator fun invoke(event: Event): Resource<Unit> {
        when(val result = eventRepository.createEvent(event)){
            is Resource.Error -> return result
            is Resource.Success -> {
                clubRepository.addEventToClub(event.categoryId, event.clubId, event.eventId)
                return result
            }
            else -> {}

        }
        return Resource.Error(Exception("Unknown error"))

    }
}
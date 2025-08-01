package com.example.sitevent.domain

import com.example.sitevent.data.Resource
import com.example.sitevent.data.repository.Inteface.EventRepository
import com.example.sitevent.data.repository.Inteface.TicketRepository
import javax.inject.Inject

class CancelTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
) {

    suspend operator fun invoke(
        categoryId: String,
        clubId: String,
        eventId: String,
        ticketId: String,
        teamId: String,
        participantIds: List<String>,
    ): Resource<Unit> {
        when (val result = ticketRepository.cancelTicket(ticketId)) {
            is Resource.Error -> return result
            is Resource.Success -> {
                eventRepository.cancelTicketInEvent(
                    categoryId,
                    clubId,
                    eventId,
                    ticketId,
                    teamId,
                    participantIds
                )
                return result
            }

            else -> {}
        }
        return Resource.Error(Exception("Unknown error"))
    }
}
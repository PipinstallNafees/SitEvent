package com.example.sitevent.domain

import com.example.sitevent.data.Resource
import com.example.sitevent.data.model.Team
import com.example.sitevent.data.model.Ticket
import com.example.sitevent.data.repository.Inteface.EventRepository
import com.example.sitevent.data.repository.Inteface.TicketRepository
import javax.inject.Inject

class UpdateTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
    ){
    suspend operator fun invoke(
        oldParticipantIds: List<String>,
        ticket: Ticket,
        team: Team
        ): Resource<Unit> {
        when(val result = ticketRepository.updateTicket(
            ticket,
            team
        )){
            is Resource.Error -> return result
            is Resource.Success -> {
                eventRepository.updateTicketInEvent(
                    ticket.categoryId,
                    ticket.clubId,
                    ticket.eventId,
                    ticket.ticketId,
                    ticket.teamId,
                    oldParticipantIds,
                    ticket.participantIds
                )
                return result
            }
            else -> {}
        }
        return Resource.Error(Exception("Unknown error"))


    }
}
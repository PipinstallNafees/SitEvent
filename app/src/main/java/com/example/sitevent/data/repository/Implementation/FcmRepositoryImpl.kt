package com.example.sitevent.data.repository.Implementation

import com.example.sitevent.Notification.FirebaseMessaging.FcmApi
import com.example.sitevent.data.model.SendMessageDto
import com.example.sitevent.data.repository.Inteface.FcmRepository
import javax.inject.Inject

class FcmRepositoryImpl @Inject constructor(
    private val api: FcmApi
) : FcmRepository {
    override suspend fun sendMessage(dto: SendMessageDto) = api.sendMessage(dto)
    override suspend fun broadcast(dto: SendMessageDto) = api.broadcast(dto)
}
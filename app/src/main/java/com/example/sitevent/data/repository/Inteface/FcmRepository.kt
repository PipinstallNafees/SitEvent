package com.example.sitevent.data.repository.Inteface

import com.example.sitevent.data.model.SendMessageDto

interface FcmRepository {
    suspend fun sendMessage(dto: SendMessageDto)
    suspend fun broadcast(dto: SendMessageDto)
}
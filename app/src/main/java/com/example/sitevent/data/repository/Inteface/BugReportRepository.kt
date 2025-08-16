package com.example.sitevent.data.repository.Inteface

import android.net.Uri
import com.example.sitevent.data.Resource

interface BugReportRepository {
    suspend fun submitBug(
        userId: String,
        title: String,
        description: String,
        appVersion: String?,
        deviceInfo: String?,
        screenshotUri: Uri?
    ): Resource<Unit>
}

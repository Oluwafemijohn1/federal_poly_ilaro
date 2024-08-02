package com.fpi.biometricsystem.data.repository

import com.fpi.biometricsystem.data.remote.FpibService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LectureRepository @Inject constructor(
    val service: FpibService,
) {
    suspend fun fetchAllLectures() = safeApiCall { service.fetchLectures() }

    suspend fun fetchCourseByCode(id: String) = safeApiCall { service.fetchCourseByCode(id) }
}
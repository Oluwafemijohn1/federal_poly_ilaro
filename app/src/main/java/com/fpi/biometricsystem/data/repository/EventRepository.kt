package com.fpi.biometricsystem.data.repository

import com.fpi.biometricsystem.data.Event
import com.fpi.biometricsystem.data.local.EventDao
import com.fpi.biometricsystem.data.remote.FpibService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    val eventDao: EventDao,
    val service: FpibService,
) {
    val allEvents = eventDao.getAllEvents()
    fun singleEvent(eventId: String) = eventDao.getSingleEvent(eventId)
    suspend fun insertEvent(event: Event) = eventDao.insert(event)

    suspend fun fetchAllEvents() = safeApiCall { service.fetchEvents() }
    suspend fun fetchExam(examNum: String) = safeApiCall { service.fetchExam(examNum) }

    suspend fun fetchEventById(id: String) = service.fetchEventById(id)
}
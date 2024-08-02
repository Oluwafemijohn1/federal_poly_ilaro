package com.fpi.biometricsystem.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.fpi.biometricsystem.data.EventInfo
import com.fpi.biometricsystem.data.ExamEvent
import com.fpi.biometricsystem.data.GenericError
import com.fpi.biometricsystem.data.GenericResponse
import com.fpi.biometricsystem.data.Lecture
import com.fpi.biometricsystem.data.LectureInfo
import com.fpi.biometricsystem.data.repository.EventRepository
import com.fpi.biometricsystem.data.repository.LectureRepository
import com.fpi.biometricsystem.utils.SingleEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventSelectionViewModel @Inject constructor(
    val eventRepository: EventRepository,
    val lectureRepository: LectureRepository
): ViewModel() {

    private val _allEvents = MutableLiveData<GenericResponse<List<EventInfo>>?>()
    private val _allExams = MutableLiveData<GenericResponse<List<ExamEvent>>?>()
    val allEvents get() = _allEvents.asFlow()
    val allExams get() = _allExams.asFlow()

    private val _allLectures = MutableLiveData<GenericResponse<List<LectureInfo>>?>()
    val allLectures get() = _allLectures.asFlow()

    private val _errorResponse: MutableLiveData<SingleEvent<GenericError?>> = MutableLiveData()
    val errorResponse: LiveData<SingleEvent<GenericError?>> get() = _errorResponse

    private val _lecture = MutableLiveData<SingleEvent<GenericResponse<List<Lecture>>?>>()
    val lecture: LiveData<SingleEvent<GenericResponse<List<Lecture>>?>> get() = _lecture

    fun fetchAllEvents() {
        viewModelScope.launch {
            val events = eventRepository.fetchAllEvents()
            if (events.isSuccessful) {
                _allEvents.postValue(events.data?.body())
            } else {
                val errorBody = events.exception
                _errorResponse.postValue(SingleEvent(errorBody))
                print("failure message: $errorBody")
            }
        }
    }

    fun fetchAllLectures() {
        viewModelScope.launch {
            val lectures = lectureRepository.fetchAllLectures()
            if (lectures.isSuccessful) {
                _allLectures.postValue(lectures.data?.body())
            } else {
                val errorBody = lectures.exception
                _errorResponse.postValue(SingleEvent(errorBody))
            }
        }
    }

    fun fetchAllExaminations() {
        viewModelScope.launch {
            val exams = eventRepository.fetchAllExams()
            println("Examinations: $exams")
            if (exams.isSuccessful) {
                _allExams.postValue(exams.data?.body())
            } else {
                val errorBody = exams.exception
                _errorResponse.postValue(SingleEvent(errorBody))
            }
        }
    }

    fun fetchCourseByCode(code: String) {
        viewModelScope.launch {
            val lecture = lectureRepository.fetchCourseByCode(code)
            if (lecture.isSuccessful){
                _lecture.postValue(SingleEvent(lecture.data?.body()))
            } else {
                val errorBody = lecture.exception
                _errorResponse.postValue(SingleEvent(errorBody))
            }
        }
    }
}
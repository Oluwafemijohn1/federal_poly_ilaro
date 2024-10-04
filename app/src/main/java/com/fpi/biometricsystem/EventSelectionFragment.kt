package com.fpi.biometricsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fpi.biometricsystem.adapters.EventListAdapter
import com.fpi.biometricsystem.adapters.ExamEventListAdapter
import com.fpi.biometricsystem.adapters.LectureListAdapter
import com.fpi.biometricsystem.adapters.OnExamItemClickListener
import com.fpi.biometricsystem.adapters.OnItemClickListener
import com.fpi.biometricsystem.adapters.OnItemClickListenerLecture
import com.fpi.biometricsystem.data.EventInfo
import com.fpi.biometricsystem.data.ExamEvent
import com.fpi.biometricsystem.data.Lecture
import com.fpi.biometricsystem.data.individual.ExamStudentInfo
import com.fpi.biometricsystem.databinding.EventSelectionFragmentBinding
import com.fpi.biometricsystem.utils.EventObserver
import com.fpi.biometricsystem.utils.hide
import com.fpi.biometricsystem.utils.makeToast
import com.fpi.biometricsystem.utils.replaceIfEmpty
import com.fpi.biometricsystem.utils.sentenceCase
import com.fpi.biometricsystem.utils.show
import com.fpi.biometricsystem.utils.showErrorDialog
import com.fpi.biometricsystem.utils.showProgressDialog
import com.fpi.biometricsystem.viewmodels.EventSelectionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class EventSelectionFragment : Fragment(), OnItemClickListener, OnItemClickListenerLecture,
    OnExamItemClickListener {

    private lateinit var binding: EventSelectionFragmentBinding
    private val viewModel: EventSelectionViewModel by activityViewModels()
    private lateinit var lecture: Lecture
    private val args: EventSelectionFragmentArgs by navArgs()
    private var exam: ExamStudentInfo? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = EventSelectionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeEvents()
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allEvents.collect {
                        requireContext().showProgressDialog(on = false)
                        val events = it?.actualData ?: emptyList()
                        val eventAdapter = EventListAdapter(events, this@EventSelectionFragment)
                        eventAdapter.notifyDataSetChanged()
                        binding.eventRecycler.adapter = eventAdapter
                    }
                }

                launch {
                    viewModel.allExams.collect {
                        requireContext().showProgressDialog(on = false)

                        exam = it?.actualData
                        binding.apply {
                            courseCode.text = "Course Code: " + exam?.course_code
                            courseName.text = "Course Name: " +  exam?.course_name
                            examNumber.text = "Exam Number: " + exam?.exam_number
                            duration.text = "Exam Duration: " + exam?.duration

                            val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val targetFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

                            exam?.exam_date?.let {
                                val date = originalFormat.parse(it)
                                val formattedDate = targetFormat.format(date)
                                examDate.text = "Exam Date: " +formattedDate
                                examDate.show()
                            }


                            examLayout.show()
                            courseCode.show()
                            courseName.show()
                            examNumber.show()
                            duration.show()
                        }
//                        val examEventsAdapter =
//                            ExamEventListAdapter(exams, this@EventSelectionFragment)
//                        examEventsAdapter.notifyDataSetChanged()
//                        binding.examRecycler.adapter = examEventsAdapter
                    }
                }

                launch {
                    viewModel.errorResponse.observe(viewLifecycleOwner, EventObserver {
                        requireContext().showProgressDialog(on = false)
                        showErrorDialog(
                            it?.message.replaceIfEmpty("Something went wrong").sentenceCase(),
                            requireContext()
                        )
                    })
                }

                launch {
                    viewModel.lecture.observe(viewLifecycleOwner, EventObserver { response ->
                        requireContext().showProgressDialog(on = false)
                        val lecture = response?.actualData.orEmpty().firstOrNull()
                        val lecturesList = response?.actualData.orEmpty()

                        if (lecturesList.isEmpty()) {
                            showErrorDialog("No Lectures scheduled", requireContext())
                            return@EventObserver
                        } else if (lecture == null) {
                            showErrorDialog("Please enter a valid lecture code", requireContext())
                            return@EventObserver
                        }
                        binding.courseWelcomeTv.text =
                            "Venue: " + lecture?.className.orEmpty()
                        val lectureAdapter =
                            LectureListAdapter(lecturesList, this@EventSelectionFragment)
                        binding.lectureRecycler.adapter = lectureAdapter
                        lectureAdapter.notifyDataSetChanged()
                    })
                }
            }
        }
    }

    private fun initViews() {
        with(binding) {
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
            eventRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
            }

//            examRecycler.apply {
//                setHasFixedSize(true)
//                layoutManager = LinearLayoutManager(requireContext())
//            }

            lectureRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
            }
            checkLectureCodeBtn.setOnClickListener {
                val lectureCode = lectureCodeEt.text.toString().trim()

                if (lectureCode.isEmpty()) {
                    requireContext().makeToast("Please enter a lecture code")
                    return@setOnClickListener
                }

                requireContext().showProgressDialog("Loading...", true)

                if (args.direction != "exam") {
                    viewModel.fetchCourseByCode(lectureCode)
                } else {
                    viewModel.fetchExamination(lectureCode)
                }
            }

            examLayout.setOnClickListener {
                exam?.let {
                    findNavController().navigate(
                        EventSelectionFragmentDirections.eventToExamination(
                            it
                        )
                    )
                }
            }

            // Check for incoming directions
            when (args.direction) {
                "student" -> {
                    toolbar.title = "Student Attendance"
                    examDiv.hide()
//                    examRecycler.hide()
                    examWelcome.hide()

                    eventDiv.hide()
                    eventRecycler.hide()
                    eventWelcomeTv.hide()
                }

                "staff" -> {
                    toolbar.title = "Staff Attendance"
                    lectureCodeEt.hide()
                    lecturesWelcomeTv.hide()
                    checkLectureCodeBtn.hide()

                    examDiv.hide()
//                    examRecycler.hide()
                    examWelcome.hide()

                    lectureRecycler.hide()
                    courseWelcomeTv.hide()

                    viewModel.fetchAllEvents()
                    requireContext().showProgressDialog("Loading...", true)
                }

                "exam" -> {
                    toolbar.title = "Examination Attendance"
                    lectureCodeEt.show()
                    lecturesWelcomeTv.show()
                    lecturesWelcomeTv.text = "Please enter Exam Number"
                    lectureCodeEt.hint = "Exam number"
                    checkLectureCodeBtn.show()
                    examLayout.show()
                    examDate.hide()
                    checkLectureCodeBtn.text = "Load Exam number"
                    lectureRecycler.hide()
                    courseWelcomeTv.hide()
                    eventDiv.hide()
                    eventRecycler.hide()
                    eventWelcomeTv.hide()
                    courseCode.hide()
                    courseName.hide()
                    examNumber.hide()
                    duration.hide()
                }
            }
        }
    }

    override fun onItemClick(item: EventInfo) {
        // Lecturer
        findNavController().navigate(
            EventSelectionFragmentDirections.actionEventSelectionFragmentToStaffAttendanceFragment(
                item
            )
        )
    }

    override fun onItemClickLecture(item: Lecture) {
        findNavController().navigate(
            EventSelectionFragmentDirections.actionEventSelectionFragmentToStudentAttendanceFragment(
                item
            )
        )
    }

    override fun onItemClick(item: ExamEvent) {
        // Examination
//        findNavController().navigate(
//            EventSelectionFragmentDirections.eventToExamination(item)
//        )
    }
}
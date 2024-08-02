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
import com.fpi.biometricsystem.databinding.EventSelectionFragmentBinding
import com.fpi.biometricsystem.utils.EventObserver
import com.fpi.biometricsystem.utils.makeToast
import com.fpi.biometricsystem.utils.replaceIfEmpty
import com.fpi.biometricsystem.utils.sentenceCase
import com.fpi.biometricsystem.utils.showErrorDialog
import com.fpi.biometricsystem.utils.showProgressDialog
import com.fpi.biometricsystem.viewmodels.EventSelectionViewModel
import kotlinx.coroutines.launch

class EventSelectionFragment : Fragment(), OnItemClickListener, OnItemClickListenerLecture,
    OnExamItemClickListener {

    private lateinit var binding: EventSelectionFragmentBinding
    private val viewModel: EventSelectionViewModel by activityViewModels()
    private lateinit var lecture: Lecture
    private val args: EventSelectionFragmentArgs by navArgs()
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
                        val exams = it?.actualData ?: emptyList()
                        val examEventsAdapter =
                            ExamEventListAdapter(exams, this@EventSelectionFragment)
                        examEventsAdapter.notifyDataSetChanged()
                        binding.examRecycler.adapter = examEventsAdapter
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

            examRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
            }

            lectureRecycler.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
            }
            checkLectureCodeBtn.setOnClickListener {
                val lectureCode = lectureCodeEt.text.toString().trim()
                if (lectureCode.isEmpty()) {
                    requireContext().makeToast("Please enter a lecture code")
                    return@setOnClickListener
                } else {
                    viewModel.fetchCourseByCode(lectureCode)
                    requireContext().showProgressDialog("Loading...", true)
                }
            }

            // Check for incoming directions
            when (args.direction) {
                "student" -> {
                    toolbar.title = "Student Attendance"
                    examDiv.visibility = View.GONE
                    examRecycler.visibility = View.GONE
                    examWelcome.visibility = View.GONE

                    eventDiv.visibility = View.GONE
                    eventRecycler.visibility = View.GONE
                    eventWelcomeTv.visibility = View.GONE
                }

                "staff" -> {
                    toolbar.title = "Staff Attendance"
                    lectureCodeEt.visibility = View.GONE
                    lecturesWelcomeTv.visibility = View.GONE
                    checkLectureCodeBtn.visibility = View.GONE

                    examDiv.visibility = View.GONE
                    examRecycler.visibility = View.GONE
                    examWelcome.visibility = View.GONE

                    lectureRecycler.visibility = View.GONE
                    courseWelcomeTv.visibility = View.GONE

                    viewModel.fetchAllEvents()
                    requireContext().showProgressDialog("Loading...", true)
                }

                "exam" -> {
                    toolbar.title = "Examination Attendance"
                    lectureCodeEt.visibility = View.GONE
                    lecturesWelcomeTv.visibility = View.GONE
                    checkLectureCodeBtn.visibility = View.GONE
                    lectureRecycler.visibility = View.GONE
                    courseWelcomeTv.visibility = View.GONE
                    eventDiv.visibility = View.GONE
                    eventRecycler.visibility = View.GONE
                    eventWelcomeTv.visibility = View.GONE
                    viewModel.fetchAllExaminations()
                    requireContext().showProgressDialog("Loading...", true)
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
        findNavController().navigate(
            EventSelectionFragmentDirections.eventToExamination(item)
        )
    }
}
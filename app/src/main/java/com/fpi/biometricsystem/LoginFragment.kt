package com.fpi.biometricsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fpi.biometricsystem.databinding.LoginFragmentBinding
import com.fpi.biometricsystem.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private lateinit var binding: LoginFragmentBinding
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.apply {
            emailEt.addTextChangedListener { loginViewModel.validateEmail(it.toString()) }
            passwordEt.addTextChangedListener { loginViewModel.validatePassword(it.toString()) }
            loginBtn.setOnClickListener { loginViewModel.login(
                emailEt.text.toString(), passwordEt.text.toString()
            )}
            observeForm()
            observeLoginResponse()
            emailErrorTv.isVisible = false
            passwordErrorTv.isVisible = false
        }
    }

    private fun observeForm() {
        lifecycleScope.launch {
            loginViewModel.loginFormData.collect {
                binding.apply {
                    loginBtn.isEnabled = it.isValidForm
                    emailErrorTv.isVisible = !it.isValidEmail
                    passwordErrorTv.isVisible = !it.isValidPassword
                }
            }
        }
    }

    private fun observeLoginResponse() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    loginViewModel.loginResponse.collect {
                        if(it) {
                            findNavController().navigate(LoginFragmentDirections.loginToHome())
                        }
                    }
                }
            }
        }
    }

}
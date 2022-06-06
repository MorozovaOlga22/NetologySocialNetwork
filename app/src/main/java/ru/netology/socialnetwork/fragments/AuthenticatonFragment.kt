package ru.netology.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.databinding.FragmentAuthenticationBinding
import ru.netology.socialnetwork.utils.AndroidUtils
import ru.netology.socialnetwork.viewmodel.AuthViewModel

@AndroidEntryPoint
class AuthenticatonFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthenticationBinding.inflate(
            inflater,
            container,
            false
        )

        with(binding) {
            login.requestFocus()

            signIn.setOnClickListener {
                AndroidUtils.hideKeyboard(requireView())
                if (login.text.isNullOrBlank() || password.text.isNullOrBlank()) {
                    error.visibility = View.VISIBLE
                    error.text = getString(R.string.emptyLoginPasswordError)
                } else {
                    error.visibility = View.GONE
                    viewModel.signIn(
                        login = login.text.toString(),
                        pass = password.text.toString()
                    )
                }
            }

            viewModel.authRespState.observe(viewLifecycleOwner) {
                if (it.isLoading) {
                    progress.visibility = View.VISIBLE
                    signIn.isEnabled = false
                } else {
                    progress.visibility = View.GONE
                    signIn.isEnabled = true
                }

                if (it.error != null) {
                    error.visibility = View.VISIBLE
                    error.text = it.error
                } else {
                    error.visibility = View.GONE
                }
            }
        }

        viewModel.authDone.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.postsFragment)
        }

        return binding.root
    }
}
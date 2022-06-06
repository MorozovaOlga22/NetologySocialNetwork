package ru.netology.socialnetwork.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.databinding.FragmentSigningBinding

@AndroidEntryPoint
class SigningFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSigningBinding.inflate(
            inflater,
            container,
            false
        )
        with(binding) {
            signIn.setOnClickListener {
                findNavController().navigate(R.id.authenticatonFragment)
            }
            signUp.setOnClickListener {
                findNavController().navigate(R.id.registrationFragment)
            }
        }

        return binding.root
    }
}
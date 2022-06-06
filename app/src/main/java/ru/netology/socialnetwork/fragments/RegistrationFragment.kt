package ru.netology.socialnetwork.fragments

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.databinding.FragmentRegistrationBinding
import ru.netology.socialnetwork.utils.AndroidUtils
import ru.netology.socialnetwork.viewmodel.AuthViewModel

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(
            inflater,
            container,
            false
        )

        with(binding) {
            name.requestFocus()
            addPhotoBinding(this)

            signUp.setOnClickListener {
                AndroidUtils.hideKeyboard(requireView())

                when {
                    !checkFields(this) -> {
                        error.visibility = View.VISIBLE
                        error.text = getString(R.string.emptyFieldsError)
                    }
                    password.text.toString() != confirmPassword.text.toString() -> {
                        error.visibility = View.VISIBLE
                        error.text = getString(R.string.differentPasswords)
                    }
                    else -> {
                        error.visibility = View.GONE
                        viewModel.signUp(
                            login = login.text.toString(),
                            pass = password.text.toString(),
                            name = name.text.toString()
                        )
                    }

                }
            }

            viewModel.authRespState.observe(viewLifecycleOwner) {
                if (it.isLoading) {
                    progress.visibility = View.VISIBLE
                    signUp.isEnabled = false
                } else {
                    progress.visibility = View.GONE
                    signUp.isEnabled = true
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

    private fun addPhotoBinding(binding: FragmentRegistrationBinding) {
        viewModel.changePhoto(null)

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changePhoto(uri)
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        with(binding) {
            removePhoto.setOnClickListener {
                viewModel.changePhoto(null)
            }

            viewModel.photo.observe(viewLifecycleOwner) {
                if (it.uri == null) {
                    photoContainer.visibility = View.GONE
                    return@observe
                }

                photoContainer.visibility = View.VISIBLE
                photo.setImageURI(it.uri)
            }
        }
    }

    private fun checkFields(binding: FragmentRegistrationBinding): Boolean {
        var allFieldsCorrect = true
        with(binding) {
            if (name.text.isNullOrBlank()) {
                name.error = getString(R.string.field_must_be_filled)
                allFieldsCorrect = false
            }
            if (login.text.isNullOrBlank()) {
                login.error = getString(R.string.field_must_be_filled)
                allFieldsCorrect = false
            }
            if (password.text.isNullOrBlank()) {
                password.error = getString(R.string.field_must_be_filled)
                allFieldsCorrect = false
            }
            if (confirmPassword.text.isNullOrBlank()) {
                confirmPassword.error = getString(R.string.field_must_be_filled)
                allFieldsCorrect = false
            }
        }
        return allFieldsCorrect
    }
}
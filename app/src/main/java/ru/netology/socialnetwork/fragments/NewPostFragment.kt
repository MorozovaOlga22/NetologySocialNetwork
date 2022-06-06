package ru.netology.socialnetwork.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.socialnetwork.R
import ru.netology.socialnetwork.databinding.FragmentNewPostEventBinding
import ru.netology.socialnetwork.enumeration.AttachmentType
import ru.netology.socialnetwork.utils.AndroidUtils
import ru.netology.socialnetwork.viewmodel.PostViewModel


@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private val postViewModel: PostViewModel by viewModels(
        ownerProducer = ::requireActivity
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostEventBinding.inflate(
            inflater,
            container,
            false
        )

        val post = postViewModel.getCurrentPost()

        with(binding) {
            edit.requestFocus()

            edit.setText(post.content)
            link.setText(post.link ?: "")
            latitude.setText(post.coords?.lat?.toString() ?: "")
            longitude.setText(post.coords?.long?.toString() ?: "")

            eventRadioGroup.visibility = View.GONE
            datetime.visibility = View.GONE

            postViewModel.loadError.observe(viewLifecycleOwner) {
                if (it.error == null) {
                    error.visibility = View.GONE
                } else {
                    error.visibility = View.VISIBLE
                    error.text = it.error
                }

                if (it.loading) {
                    savePost.visibility = View.GONE
                    progress.visibility = View.VISIBLE
                } else {
                    savePost.visibility = View.VISIBLE
                    progress.visibility = View.GONE
                }
            }

            addAttachment(this)

            savePost.setOnClickListener {
                postViewModel.updateContent(edit.text.toString())
                postViewModel.updateLink(link.text.toString())
                postViewModel.updateCoords(
                    latitude.text.toString(),
                    latitude.text.toString()
                )
                postViewModel.updateAttachment(urlString.text.toString())
                if (checkFields(this)) {
                    postViewModel.createOrUpdate()
                    AndroidUtils.hideKeyboard(requireView())
                } else {
                    error.visibility = View.VISIBLE
                    error.text = getString(R.string.fill_fields_correctly)
                }
            }
        }

        postViewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun addAttachment(binding: FragmentNewPostEventBinding) {
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
                    Activity.RESULT_OK -> postViewModel.changeAttachmentUri(it.data?.data)
                }
            }

        binding.selectAttachmentType.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.options_attachment)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.none -> {
                            postViewModel.changeAttachmentType(null)
                            true
                        }
                        R.id.photo -> {
                            postViewModel.changeAttachmentType(AttachmentType.IMAGE)
                            true
                        }
                        R.id.video -> {
                            postViewModel.changeAttachmentType(AttachmentType.VIDEO)
                            true
                        }
                        R.id.audio -> {
                            postViewModel.changeAttachmentType(AttachmentType.AUDIO)
                            true
                        }

                        else -> false
                    }
                }
            }.show()
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
                postViewModel.changeAttachmentUri(null)
            }

            postViewModel.attachmentModel.observe(viewLifecycleOwner) {
                if (it.url == null) {
                    photo.visibility = View.GONE
                    return@observe
                }

                photo.visibility = View.VISIBLE
                photo.setImageURI(it.url)
            }

            postViewModel.attachmentModel.observe(viewLifecycleOwner) {
                when (it.type) {
                    null -> {
                        selectAttachmentType.text = getString(R.string.none)
                        urlString.visibility = View.GONE
                        photoPanel.visibility = View.GONE
                    }
                    AttachmentType.IMAGE -> {
                        selectAttachmentType.text = getString(R.string.photo)
                        urlString.visibility = View.VISIBLE
                        photoPanel.visibility = View.VISIBLE
                    }
                    AttachmentType.VIDEO -> {
                        selectAttachmentType.text = getString(R.string.video)
                        urlString.visibility = View.VISIBLE
                        photoPanel.visibility = View.GONE
                    }
                    AttachmentType.AUDIO -> {
                        selectAttachmentType.text = getString(R.string.audio)
                        urlString.visibility = View.VISIBLE
                        photoPanel.visibility = View.GONE
                    }
                }

                urlString.setText(it.urlString)
            }
        }
    }

    private fun checkFields(binding: FragmentNewPostEventBinding): Boolean {
        var allFieldsCorrect = true

        with(binding) {
            if (latitude.text.isNullOrBlank()) {
                if (!longitude.text.isNullOrBlank()) {
                    latitude.error = getString(R.string.field_must_be_filled)
                    allFieldsCorrect = false
                }
            } else if (latitude.text.toString()
                    .toDouble() > 90 || latitude.text.toString().toDouble() < -90
            ) {
                latitude.error = getString(R.string.latitude_error)
                allFieldsCorrect = false
            }
            if (longitude.text.isNullOrBlank()) {
                if (!latitude.text.isNullOrBlank()) {
                    longitude.error = getString(R.string.field_must_be_filled)
                    allFieldsCorrect = false
                }
            } else if (longitude.text.toString()
                    .toDouble() > 180 || longitude.text.toString().toDouble() < -180
            ) {
                longitude.error = getString(R.string.longitude_error)
                allFieldsCorrect = false
            }

            val attachment =
                postViewModel.attachmentModel.value
                    ?: throw RuntimeException("Can't get attachment")
            if (attachment.type != null && attachment.url == null && attachment.urlString == null) {
                urlString.error = getString(R.string.enter_url_or_pick_attachment)
                allFieldsCorrect = false
            }
        }

        return allFieldsCorrect
    }
}
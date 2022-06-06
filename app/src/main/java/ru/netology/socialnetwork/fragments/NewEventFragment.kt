package ru.netology.socialnetwork.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.RadioGroup
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
import ru.netology.socialnetwork.enumeration.EventType
import ru.netology.socialnetwork.utils.AndroidUtils
import ru.netology.socialnetwork.viewmodel.EventViewModel


@AndroidEntryPoint
class NewEventFragment : Fragment() {
    private val eventViewModel: EventViewModel by viewModels(
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

        val event = eventViewModel.getCurrentEvent()

        with(binding) {
            edit.requestFocus()

            edit.setText(event.content)
            link.setText(event.link)

            latitude.setText(event.coords?.lat?.toString() ?: "")
            longitude.setText(event.coords?.long?.toString() ?: "")

            eventRadioGroup.check(if (event.type == EventType.OFFLINE) R.id.radio_button_offline else R.id.radio_button_online)

            datetime.setText(event.datetime ?: "")

            eventViewModel.loadError.observe(viewLifecycleOwner) {
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
                eventViewModel.updateContent(edit.text.toString())
                eventViewModel.updateLink(link.text.toString())
                eventViewModel.updateCoords(
                    latitude.text.toString(),
                    latitude.text.toString()
                )
                eventViewModel.updateAttachment(urlString.text.toString())
                eventViewModel.updateType(getEventType(eventRadioGroup))
                eventViewModel.updateDatetime(datetime.text.toString())
                if (checkFields(this)) {
                    eventViewModel.createOrUpdate()
                    AndroidUtils.hideKeyboard(requireView())
                } else {
                    error.visibility = View.VISIBLE
                    error.text = getString(R.string.fill_fields_correctly)
                }
            }
        }

        eventViewModel.eventCreated.observe(viewLifecycleOwner) {
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
                    Activity.RESULT_OK -> eventViewModel.changeAttachmentUri(it.data?.data)
                }
            }

        binding.selectAttachmentType.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.options_attachment)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.none -> {
                            eventViewModel.changeAttachmentType(null)
                            true
                        }
                        R.id.photo -> {
                            eventViewModel.changeAttachmentType(AttachmentType.IMAGE)
                            true
                        }
                        R.id.video -> {
                            eventViewModel.changeAttachmentType(AttachmentType.VIDEO)
                            true
                        }
                        R.id.audio -> {
                            eventViewModel.changeAttachmentType(AttachmentType.AUDIO)
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
                eventViewModel.changeAttachmentUri(null)
            }

            eventViewModel.attachmentModel.observe(viewLifecycleOwner) {
                if (it.url == null) {
                    photo.visibility = View.GONE
                    return@observe
                }

                photo.visibility = View.VISIBLE
                photo.setImageURI(it.url)
            }

            eventViewModel.attachmentModel.observe(viewLifecycleOwner) {
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
                eventViewModel.attachmentModel.value
                    ?: throw RuntimeException("Can't get attachment")
            if (attachment.type != null && attachment.url == null && attachment.urlString == null) {
                urlString.error = getString(R.string.enter_url_or_pick_attachment)
                allFieldsCorrect = false
            }

            if (eventRadioGroup.checkedRadioButtonId == View.NO_ID) {
                allFieldsCorrect = false
            }
        }

        return allFieldsCorrect;
    }

    private fun getEventType(eventRadioGroup: RadioGroup): EventType {
        return when (eventRadioGroup.checkedRadioButtonId) {
            R.id.radio_button_online -> EventType.ONLINE
            else -> EventType.OFFLINE
        }
    }
}

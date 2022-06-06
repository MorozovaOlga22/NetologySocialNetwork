package ru.netology.socialnetwork

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.socialnetwork.databinding.ActivityMainBinding
import ru.netology.socialnetwork.viewmodel.AuthViewModel
import ru.netology.socialnetwork.viewmodel.UsersViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val authViewModel: AuthViewModel by viewModels()
        val usersViewModel: UsersViewModel by viewModels()

        with(binding) {
            authViewModel.data.observe(this@MainActivity) { data ->
                val authenticated: Boolean = data.id != 0L
                signingButton.visibility = if (authenticated) View.GONE else View.VISIBLE
                logOutButton.visibility = if (authenticated) View.VISIBLE else View.GONE

                if (authenticated) {
                    myProfileButton.visibility = View.VISIBLE
                    myProfileButton.setOnClickListener {
                        usersViewModel.getUserById(data.id)
                        findNavController(R.id.nav_host_fragment).navigate(R.id.userProfileFragment)
                    }
                } else {
                    myProfileButton.visibility = View.GONE
                }
            }

            postsButton.setOnClickListener {
                findNavController(R.id.nav_host_fragment).navigate(R.id.postsFragment)
            }

            eventsButton.setOnClickListener {
                findNavController(R.id.nav_host_fragment).navigate(R.id.eventsFragment)
            }

            usersButton.setOnClickListener {
                usersViewModel.getUsers()
                findNavController(R.id.nav_host_fragment).navigate(R.id.usersFragment)
            }

            signingButton.setOnClickListener {
                findNavController(R.id.nav_host_fragment).navigate(R.id.signingFragment)
            }

            logOutButton.setOnClickListener {
                authViewModel.logOut()
                findNavController(R.id.nav_host_fragment).navigate(R.id.postsFragment)
            }
        }
    }
}
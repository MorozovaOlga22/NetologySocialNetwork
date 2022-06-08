package ru.netology.socialnetwork.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.socialnetwork.R

fun ImageView.loadPhoto(url: String) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .timeout(10_000)
        .into(this)
}
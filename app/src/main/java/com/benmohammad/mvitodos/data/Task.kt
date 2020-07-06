package com.benmohammad.mvitodos.data

import com.benmohammad.mvitodos.util.isNotNullNorEmpty
import java.util.*

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String?,
    val description: String?,
    val completed: Boolean = false
) {

    val titleForList =
        if(title.isNotNullNorEmpty()) {
            title
        } else {
            description
        }

    val active = !completed

    val empty = title.isNullOrEmpty() && description.isNullOrEmpty()
}
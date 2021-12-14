package org.eldom.model

import kotlinx.serialization.Serializable

sealed class Task {
    abstract val name: String
}

@Serializable
data class CreateTask (override val name: String): Task()

@Serializable
data class UpdateTask (val id: Int, override val name: String): Task()

@Serializable
data class ReadTask (val id: Int, override val name: String) : Task()


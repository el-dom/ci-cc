package org.eldom.model

import kotlinx.serialization.Serializable

sealed class Todo {
    abstract val name: String
    abstract val description: String
    abstract val tasks: List<Task>
}

@Serializable
data class CreateTodo(
    override val name: String,
    override val description: String,
    override val tasks: List<CreateTask> = emptyList()
) : Todo()

@Serializable
data class ReadTodo(
    val id: Int,
    override val name: String,
    override val description: String,
    override val tasks: List<ReadTask>
) : Todo()

@Serializable
data class UpdateTodo(
    val id: Int,
    override val name: String,
    override val description: String,
    override val tasks: List<CreateTask>
) : Todo()

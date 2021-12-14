package org.eldom.db.dao

import org.eldom.model.ReadTodo
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Todos : IntIdTable() {
    val name = varchar("name", 255)
    val description = varchar("description", 255)
}

class TodoEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TodoEntity>(Todos)

    var name by Todos.name
    var description by Todos.description
    val tasks by TaskEntity referrersOn Tasks.todoId

    fun toTodo() = ReadTodo(id.value, name, description, tasks.map { it.toTask() })
}
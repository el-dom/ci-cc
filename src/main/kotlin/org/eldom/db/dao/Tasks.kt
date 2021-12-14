package org.eldom.db.dao

import org.eldom.model.ReadTask
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Tasks : IntIdTable() {
    val name = varchar("name", 255)
    val todoId = reference(name = "todoId", foreign = Todos, onDelete = ReferenceOption.CASCADE)
}


class TaskEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TaskEntity>(Tasks)

    var name by Tasks.name
    var todo by TodoEntity referencedOn Tasks.todoId

    fun toTask() = ReadTask(id.value, name)
}
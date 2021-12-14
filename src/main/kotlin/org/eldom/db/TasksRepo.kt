package org.eldom.db

import org.eldom.db.dao.TaskEntity
import org.eldom.db.dao.Tasks
import org.eldom.db.dao.TodoEntity
import org.eldom.db.dao.Todos
import org.eldom.model.CreateTask
import org.eldom.model.ReadTask
import org.eldom.model.UpdateTask
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class TasksRepo {

    suspend fun getAllTasksForTodo(todoId: Int): List<ReadTask> = newSuspendedTransaction {
        TaskEntity.wrapRows(Tasks.select { Tasks.todoId eq todoId }).map { it.toTask() }
    }

    suspend fun createTaskForTodo(todoId: Int, task: CreateTask): ReadTask? = newSuspendedTransaction {
        Todos.select { Todos.id eq todoId }.singleOrNull()?.let { TodoEntity.wrapRow(it) }?.let { todoEntity ->
            TaskEntity.new {
                this.name = task.name
                this.todo = todoEntity
            }.toTask()
        }
    }

    suspend fun getTaskById(id: Int): ReadTask? = newSuspendedTransaction {
        getTaskEntityById(id)?.toTask()
    }


    suspend fun updateTask(id: Int, task: UpdateTask): ReadTask? = newSuspendedTransaction {
        getTaskEntityById(id)?.let { taskEntity ->
            taskEntity.name = task.name
            taskEntity.toTask()
        }
    }

    suspend fun deleteTaskById(id: Int): Boolean = newSuspendedTransaction {
        Tasks.deleteWhere { Tasks.id eq id } > 0
    }

    private suspend fun getTaskEntityById(id: Int): TaskEntity? = newSuspendedTransaction {
        Tasks.select { Tasks.id eq id }.singleOrNull()?.let { TaskEntity.wrapRow(it) }
    }
}
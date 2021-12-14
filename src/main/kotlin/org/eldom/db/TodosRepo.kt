package org.eldom.db

import org.eldom.db.dao.TaskEntity
import org.eldom.db.dao.Tasks
import org.eldom.db.dao.TodoEntity
import org.eldom.db.dao.Todos
import org.eldom.model.CreateTask
import org.eldom.model.CreateTodo
import org.eldom.model.ReadTodo
import org.eldom.model.UpdateTodo
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class TodosRepo {

    suspend fun getAllTodos(): List<ReadTodo> = newSuspendedTransaction {
        TodoEntity.wrapRows(Todos.selectAll()).map{it.toTodo()}
    }

    suspend fun createTodo(todo: CreateTodo): ReadTodo = newSuspendedTransaction {
        TodoEntity.new {
            this.name = todo.name
            this.description = todo.description
        }.also { todoEntity ->
            writeTasksForTodo(todo.tasks, todoEntity)
        }.toTodo()
    }

    suspend fun getTodoById(id: Int): ReadTodo? = newSuspendedTransaction {
        Todos.select { Todos.id eq id }.firstOrNull()?.let { TodoEntity.wrapRow(it) }?.toTodo()
    }

    suspend fun updateTodo(id: Int, todo: UpdateTodo): ReadTodo? = newSuspendedTransaction {
        getTodoEntityById(id)?.let { todoEntity ->
            todoEntity.name = todo.name
            todoEntity.description = todo.description

            Tasks.deleteWhere { Tasks.todoId eq id }

            writeTasksForTodo(todo.tasks, todoEntity)
            todoEntity.toTodo()
        }
    }

    suspend fun deleteTodoById(id: Int): Boolean = newSuspendedTransaction {
        Todos.deleteWhere { Todos.id eq id } > 0
    }

    private suspend fun getTodoEntityById(id: Int): TodoEntity? = newSuspendedTransaction {
        val query = Todos.join(Tasks, JoinType.INNER, additionalConstraint = { Todos.id eq Tasks.todoId })
            .select { Todos.id eq id }
        TodoEntity.wrapRows(query).firstOrNull()
    }

    private fun writeTasksForTodo(tasks: List<CreateTask>, todoEntity: TodoEntity) {
        tasks.forEach {
            TaskEntity.new {
                this.name = it.name
                this.todo = todoEntity
            }
        }
    }
}

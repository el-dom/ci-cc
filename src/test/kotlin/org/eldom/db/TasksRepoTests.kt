package org.eldom.db

import kotlinx.coroutines.runBlocking
import org.eldom.db.dao.TaskEntity
import org.eldom.db.dao.Tasks
import org.eldom.db.dao.TodoEntity
import org.eldom.db.dao.Todos
import org.eldom.model.CreateTask
import org.eldom.util.TestDatabaseFactory
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.module
import org.koin.test.inject
import org.koin.test.junit5.AutoCloseKoinTest
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.assertEquals

class TasksRepoTests : AutoCloseKoinTest() {

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single<DatabaseFactory> { TestDatabaseFactory() }
                single { TodosRepo() }
                single { TasksRepo() }
            }
        )
    }

    private val dbFactory: DatabaseFactory by inject()
    private val tasksRepo: TasksRepo by inject()

    @BeforeEach
    fun setup() {
        dbFactory.connect()
        runBlocking {
            transaction {
                (1..3).map { id ->
                    TodoEntity.new {
                        this.name = "todo $id"
                        this.description = "todo $id description"
                    }.also { todoEntity ->
                        TaskEntity.new {
                            this.name = "task $id.1 name"
                            this.todo = todoEntity
                        }
                        TaskEntity.new {
                            this.name = "task $id.2 name"
                            this.todo = todoEntity
                        }
                    }
                }
            }
        }
    }

    @AfterEach
    fun teardown() {
        dbFactory.close()
    }

    @Test
    fun `get all Tasks`() {
        runBlocking {
            val todoID = transaction {
                TodoEntity.wrapRow(Todos.selectAll().first()).id.value
            }
            val tasksRes = newSuspendedTransaction {
                tasksRepo.getAllTasksForTodo(todoID)
            }
            assertEquals(2, tasksRes.size)
            assertEquals("task 1.1 name, task 1.2 name", tasksRes.joinToString { it.name })
        }
    }

    @Test
    fun `create new Task`() =
        runBlocking {
            val todoID = transaction {
                TodoEntity.wrapRow(Todos.selectAll().last()).id.value
            }
            newSuspendedTransaction {
                tasksRepo.createTaskForTodo(
                    todoID,
                    CreateTask("new task")
                )
                val tasksRes = TaskEntity.wrapRows(Tasks.select { Tasks.todoId eq todoID }).map { it.toTask() }
                assertEquals(3, tasksRes.size)
                assertEquals("task 3.1 name, task 3.2 name, new task", tasksRes.joinToString { it.name })
            }
        }

    @Test
    fun `get Task`() = runBlocking {
        val task = transaction {
            TaskEntity.wrapRow(Tasks.selectAll().first())
        }
        val taskRes = newSuspendedTransaction {
            tasksRepo.getTaskById(task.id.value)
        }
        assertEquals(taskRes?.name, task.name)

    }

    @Test
    fun `delete Task`()= runBlocking {
        val task = transaction {
            TaskEntity.wrapRow(Tasks.selectAll().last())
        }
        val deleteRes = newSuspendedTransaction {
            tasksRepo.deleteTaskById(task.id.value)
        }
        assertEquals(true, deleteRes)

        val count = transaction {
            Tasks.selectAll().count()
        }
        assertEquals(5, count)
    }

    @Test
    fun `querying unknow Task returns null`()= runBlocking {
        val res = newSuspendedTransaction {
            tasksRepo.getTaskById(-1)
        }
        assertEquals(null, res)
    }
}
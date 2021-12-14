package org.eldom.db

import kotlinx.coroutines.runBlocking
import org.eldom.db.dao.TaskEntity
import org.eldom.db.dao.TodoEntity
import org.eldom.db.dao.Todos
import org.eldom.model.CreateTask
import org.eldom.model.CreateTodo
import org.eldom.model.UpdateTodo
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

class TodosRepoTests : AutoCloseKoinTest() {

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
    private val todosRepo: TodosRepo by inject()

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
    fun `get all Todos`() {
        runBlocking {
            val todosRes = newSuspendedTransaction {
                todosRepo.getAllTodos()
            }
            assertEquals(3, todosRes.size)
            assertEquals(6, todosRes.flatMap { it.tasks }.size)
            assertEquals("todo 1, todo 2, todo 3", todosRes.joinToString { it.name })
            assertEquals(
                "task 1.1 name, task 1.2 name, task 2.1 name, task 2.2 name, task 3.1 name, task 3.2 name",
                todosRes.flatMap { it.tasks.map { task -> task.name } }.joinToString()
            )
        }
    }

    @Test
    fun `create new Todo`() =
        runBlocking {
            newSuspendedTransaction {
                val createdTodo = todosRepo.createTodo(
                    CreateTodo(
                        "new todo",
                        "new description",
                        listOf(CreateTask("task 1"), CreateTask("task 2"))
                    )
                )
                val todoRes = TodoEntity.wrapRow(Todos.select { Todos.id eq createdTodo.id }.first())
                assertEquals(createdTodo.name, todoRes.name)
            }
        }

    @Test
    fun `get Todo`() {
        runBlocking {
            val todoID = transaction {
                TodoEntity.wrapRow(Todos.selectAll().first()).id.value
            }
            val todoRes = todosRepo.getTodoById(todoID)!!
            assertEquals("todo $todoID", todoRes.name)
            assertEquals("todo $todoID description", todoRes.description)
            assertEquals("task ${todoID}.1 name, task ${todoID}.2 name", todoRes.tasks.joinToString { it.name })
        }
    }

    @Test
    fun `update Todo`() {
        runBlocking {
            val todoID = transaction {
                TodoEntity.wrapRow(Todos.selectAll().last()).id.value
            }
            val todoRes = newSuspendedTransaction {
                todosRepo.updateTodo(
                    todoID,
                    UpdateTodo(
                        todoID,
                        "totally different name",
                        "totally different description",
                        listOf(CreateTask("sole task"))
                    )
                )
                TodoEntity.wrapRow(Todos.select { Todos.id eq todoID }.first()).toTodo()
            }
            assertEquals("totally different name", todoRes.name)
            assertEquals("totally different description", todoRes.description)
            assertEquals("sole task", todoRes.tasks.joinToString { it.name })
        }
    }

    @Test
    fun `delete Todo`() {
        runBlocking {
            val todoID = transaction {
                TodoEntity.wrapRow(Todos.selectAll().first()).id.value
            }
            val deleteRes = todosRepo.deleteTodoById(todoID)
            assertEquals(true, deleteRes)
            val count = transaction {
                Todos.selectAll().count()
            }
            assertEquals(2, count)
        }
    }

    @Test
    fun `querying unknow Todo returns null`()= runBlocking {
        val res = newSuspendedTransaction {
            todosRepo.getTodoById(-1)
        }
        assertEquals(null, res)
    }
}
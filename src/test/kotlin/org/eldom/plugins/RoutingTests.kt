package org.eldom.plugins

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eldom.db.TasksRepo
import org.eldom.db.TodosRepo
import org.eldom.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.module
import org.koin.test.junit5.AutoCloseKoinTest
import org.koin.test.junit5.KoinTestExtension
import org.koin.test.junit5.mock.MockProviderExtension
import org.koin.test.mock.declareMock
import kotlin.test.assertEquals

@Suppress("unused")
class RoutingTests : AutoCloseKoinTest() {

    private val task1 = ReadTask(1, "one")
    private val task2 = ReadTask(2, "two")
    private val task3 = ReadTask(3, "three")
    private val task4 = ReadTask(4, "four")

    private val todo1 = ReadTodo(1, "one", "one desc", listOf(task1, task2))
    private val todo2 = ReadTodo(2, "two", "two desc", listOf(task3, task4))

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { TodosRepo() }
                single { TasksRepo() }
            }
        )
    }

    @JvmField
    @RegisterExtension
    val koinMockProvider = MockProviderExtension.create { mockkClass(it) }

    @BeforeEach
    fun initMocks() {

        declareMock<TodosRepo> {

            coEvery { this@declareMock.getAllTodos() } returns listOf(
                todo1,
                todo2
            )

            coEvery { this@declareMock.getTodoById(1) } returns todo1

            coEvery { this@declareMock.getTodoById(2) } returns todo2

            coEvery { this@declareMock.createTodo(any<CreateTodo>()) } answers {
                createReadFromCreateTodo(firstArg())
            }

            coEvery { this@declareMock.updateTodo(any(), any<UpdateTodo>()) } answers {
                createReadFromCreateTodo(secondArg())
            }

            coEvery { this@declareMock.deleteTodoById(any()) } returns true

        }
        declareMock<TasksRepo> {
            coEvery { this@declareMock.getAllTasksForTodo(1) } returns listOf(
                task1,
                task2
            )
            coEvery { this@declareMock.createTaskForTodo(2, any<CreateTask>()) } answers {
                ReadTask(1, (secondArg() as CreateTask).name)
            }

            coEvery { this@declareMock.getTaskById(1) } returns task1

            coEvery { this@declareMock.updateTask(any(), any<UpdateTask>()) } answers {
                createReadFromCreateTask(secondArg())
            }

            coEvery { this@declareMock.deleteTaskById(1) } returns true
            coEvery { this@declareMock.deleteTaskById(10) } returns false
        }
    }

    @Test
    fun `get all Todos`() {
        withTestApplication(Application::configureRouting) {
            handleRequest(HttpMethod.Get, "/todos").apply {
                val expected = Json.encodeToString(listOf(todo1, todo2))
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(expected, response.content)
            }
        }
    }

    @Test
    fun `create new Todo`() = withTestApplication(Application::configureRouting) {
        with(handleRequest(HttpMethod.Post, "/todos") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateTodo("todo name", "description", listOf(CreateTask("task name")))))
        }) {
            val expected =
                Json.encodeToString(ReadTodo(99, "todo name", "description", listOf(ReadTask(1, "task name"))))
            assertEquals(HttpStatusCode.Created, response.status())
            assertEquals(expected, response.content)
        }
    }

    @Test
    fun `get Todo`() {
        withTestApplication(Application::configureRouting) {
            handleRequest(HttpMethod.Get, "/todos/1").apply {
                val expected = Json.encodeToString(todo1)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(expected, response.content)
            }
        }
    }

    @Test
    fun `update Todo`() {
        withTestApplication(Application::configureRouting) {
            with(handleRequest(HttpMethod.Put, "/todos/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    Json.encodeToString(
                        UpdateTodo(
                            1,
                            "todo name",
                            "description",
                            listOf(CreateTask("task name"))
                        )
                    )
                )
            }) {
                val expected =
                    Json.encodeToString(ReadTodo(1, "todo name", "description", listOf(ReadTask(1, "task name"))))
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(expected, response.content)
            }
        }
    }

    @Test
    fun `delete Todo`() {
        withTestApplication(Application::configureRouting) {
            handleRequest(HttpMethod.Delete, "/todos/1").apply {
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
        }
    }

    @Test
    fun `get all Tasks`() {
        withTestApplication(Application::configureRouting) {
            handleRequest(HttpMethod.Get, "/todos/1/tasks").apply {
                val expected = Json.encodeToString(listOf(task1, task2))
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(expected, response.content)
            }
        }
    }

    @Test
    fun `create new Task`() = withTestApplication(Application::configureRouting) {
        with(handleRequest(HttpMethod.Post, "/todos/2/tasks") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(CreateTask("task name")))
        }) {
            val expected = Json.encodeToString(ReadTask(1, "task name"))
            assertEquals(HttpStatusCode.Created, response.status())
            assertEquals(expected, response.content)
        }
    }

    @Test
    fun `get Task`() {
        withTestApplication(Application::configureRouting) {
            handleRequest(HttpMethod.Get, "/tasks/1").apply {
                val expected = Json.encodeToString(task1)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(expected, response.content)
            }
        }
    }

    @Test
    fun `update Task`() {
        withTestApplication(Application::configureRouting) {
            with(handleRequest(HttpMethod.Put, "/tasks/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    Json.encodeToString(
                        UpdateTask(
                            1,
                            "description"
                        )
                    )
                )
            }) {
                val expected = Json.encodeToString(ReadTask(1, "description"))
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(expected, response.content)
            }
        }
    }

    @Test
    fun `delete Task`() {
        withTestApplication(Application::configureRouting) {
            handleRequest(HttpMethod.Delete, "/tasks/1").apply {
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
        }
    }

    @Test
    fun `answer 404 on unknown task`() {
        withTestApplication(Application::configureRouting) {
            handleRequest(HttpMethod.Delete, "/tasks/10").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    private fun <T : Todo> createReadFromCreateTodo(arg: T) = ReadTodo(
        if (arg is UpdateTodo) arg.id else 99,
        arg.name,
        arg.description,
        arg.tasks.mapIndexed { index, elem -> ReadTask(index + 1, elem.name) }
    )

    private fun <T : Task> createReadFromCreateTask(arg: T) = ReadTask(
        if (arg is UpdateTask) arg.id else 99,
        arg.name
    )

}
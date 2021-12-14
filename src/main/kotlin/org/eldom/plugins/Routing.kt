package org.eldom.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.request.ContentTransformationException
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.util.pipeline.*
import org.eldom.db.TasksRepo
import org.eldom.db.TodosRepo
import org.eldom.model.CreateTask
import org.eldom.model.UpdateTask
import org.eldom.model.UpdateTodo
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }
    install(StatusPages) {
        exception<ContentTransformationException> { cte ->
            call.respondText(
                text = cte.message ?: "Unknown error",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest
            )
        }
        exception<NumberFormatException> { cte ->
            call.respondText(
                text = cte.message ?: "Unknown error",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest
            )
        }
        exception<Throwable> { cause ->
            call.respondText(
                text = cause.message ?: "Unknown error",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.InternalServerError
            )
        }
    }

    val todosRepo by inject<TodosRepo>()
    val taskRepo by inject<TasksRepo>()

    routing {
        //Todos
        get("/todos") {
            val todos = todosRepo.getAllTodos()
            if (todos.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, todos)
            } else {
                call.respond(HttpStatusCode.NotFound, todos)
            }
        }
        post("/todos") {
            val todo = todosRepo.createTodo(call.receive())
            call.response.headers.append(HttpHeaders.Location, "/todos/${todo.id}")
            call.respond(HttpStatusCode.Created, todo)
        }
        get("/todos/{id}") {
            val id = call.parameters["id"] ?: return@get handleMissingId()
            val todo =
                todosRepo.getTodoById(id.toInt()) ?: return@get handleUnknownId(id)
            call.respond(todo)
        }
        put("/todos/{id}") {
            val todoId = call.parameters["id"] ?: return@put handleMissingId()
            val todo = call.receive<UpdateTodo>()
            if(todoId.toInt() != todo.id) {
                call.respondText(text = "IDs in url and body don't match", status = HttpStatusCode.BadRequest)
            }
            val updatedTodo = todosRepo.updateTodo(todoId.toInt(), todo)
            if (updatedTodo != null) {
                call.respond(HttpStatusCode.OK, updatedTodo)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        delete("/todos/{id}") {
            val todoId = call.parameters["id"] ?: return@delete handleMissingId()
            if (todosRepo.deleteTodoById(todoId.toInt())) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        //Tasks
        get("/todos/{id}/tasks") {
            val todoId = call.parameters["id"] ?: return@get handleMissingId()
            val tasks = taskRepo.getAllTasksForTodo(todoId.toInt())
            if (tasks.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, tasks)
            } else {
                call.respond(HttpStatusCode.NotFound, tasks)
            }
        }
        post("/todos/{todoId}/tasks") {
            val todoId = call.parameters["todoId"] ?: return@post handleMissingId()
            val task = call.receive<CreateTask>()
            val createdTask = taskRepo.createTaskForTodo(todoId.toInt(), task)
            if (createdTask != null) {
                call.response.headers.append(HttpHeaders.Location, "/todos/$todoId/tasks/${createdTask.id}")
                call.respond(HttpStatusCode.Created, createdTask)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        get("/tasks/{taskId}") {
            val taskId = call.parameters["taskId"] ?: return@get handleMissingId()
            val task = taskRepo.getTaskById(taskId.toInt())
            if (task != null) {
                call.respond(HttpStatusCode.OK, task)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        put("/tasks/{taskId}") {
            val taskId = call.parameters["taskId"] ?: return@put handleMissingId()
            val task = call.receive<UpdateTask>()
            if(taskId.toInt() != task.id) {
                call.respondText(text = "IDs in url and body don't match", status = HttpStatusCode.BadRequest)
            }
            val updatedTask = taskRepo.updateTask(taskId.toInt(), task)
            if (updatedTask != null) {
                call.respond(HttpStatusCode.OK, task)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        delete("/tasks/{taskId}") {
            val taskId = call.parameters["taskId"] ?: return@delete handleMissingId()
            if (taskRepo.deleteTaskById(taskId.toInt())) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        static("docs") {
            files("build/swagger-ui-api")
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleUnknownId(id: String) {
    call.respondText(
        text = "No todo with id $id",
        status = HttpStatusCode.NotFound
    )
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleMissingId() {
    return call.respondText(
        text = "Missing or malformed id",
        status = HttpStatusCode.BadRequest
    )
}

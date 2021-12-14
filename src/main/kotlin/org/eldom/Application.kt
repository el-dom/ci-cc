package org.eldom

import io.ktor.application.*
import io.ktor.server.cio.*
import org.eldom.db.DatabaseFactory
import org.eldom.db.dao.Tasks
import org.eldom.db.dao.Todos
import org.eldom.di.appModule
import org.eldom.plugins.configureRouting
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {

    install(Koin) {
        modules(appModule)
    }

    val databaseFactory by inject<DatabaseFactory>()
    databaseFactory.connect()
    transaction {
        SchemaUtils.create(Todos, Tasks)
    }
    configureRouting()
}

//fun initDb(): Database {
//    val db = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")
//    transaction(db) {
//        SchemaUtils.create(Todos, Tasks)
//    }
//    return db
//    val config = HikariConfig("resources/db/hikari.properties")
//    val ds = HikariDataSource(config)
//    Database.connect(ds)
//}

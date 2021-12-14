package org.eldom.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.eldom.db.DatabaseFactory
import org.eldom.db.dao.Tasks
import org.eldom.db.dao.Todos
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class TestDatabaseFactory : DatabaseFactory {
    lateinit var source: HikariDataSource

    override fun close() {
        source.close()
    }

    override fun connect() {
        Database.connect(hikari())
        transaction {
            SchemaUtils.create(Todos, Tasks)
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.h2.Driver"
        config.jdbcUrl = "jdbc:h2:mem:;DATABASE_TO_UPPER=false;MODE=PostgreSQL"
        config.maximumPoolSize = 2
        config.isAutoCommit = true
        config.validate()
        source = HikariDataSource(config)
        return source
    }
}

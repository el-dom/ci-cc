package org.eldom.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

class DatabaseFactoryImpl : DatabaseFactory {

    override fun close() {
        // not necessary
    }

    override fun connect() {
        Database.connect(hikari())
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig("/db/hikari.properties")
        config.schema = "public"
        return HikariDataSource(config)
    }
}
package org.eldom.db

interface DatabaseFactory {
    fun connect()
    fun close()
}
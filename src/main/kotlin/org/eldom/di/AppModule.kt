package org.eldom.di

import org.eldom.db.DatabaseFactory
import org.eldom.db.DatabaseFactoryImpl
import org.eldom.db.TasksRepo
import org.eldom.db.TodosRepo
import org.koin.dsl.module

val appModule = module {
    single<DatabaseFactory> { DatabaseFactoryImpl() }
    single { TasksRepo() }
    single { TodosRepo() }
}
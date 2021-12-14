rootProject.name = "org.eldom.ci-cc"

pluginManagement {
    val kotlinVersion: String by settings
    val swaggerGenVersion: String by settings
    val kotlinxSerializationPluginVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.hidetake.swagger.generator") version swaggerGenVersion
        kotlin("plugin.serialization") version kotlinxSerializationPluginVersion
    }
}


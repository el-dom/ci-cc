val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val jupiterVersion: String by project
val mockkVersion: String by project
val kotlinxSerializationJsonVersion: String by project
val exposedVersion: String by project
val swaggerUIVersion: String by project
val hikariVersion: String by project
val postgresqlVersion: String by project
val koinVersion: String by project
val h2Version: String by project

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.hidetake.swagger.generator")
}

group = "org.eldom"
version = "1.0.0"
application {
    mainClass.set("org.eldom.ApplicationKt")
}

repositories {
    mavenCentral()
}

swaggerSources {
    register("api").configure {
        setInputFile(projectDir.resolve("openapi.yaml"))
    }
}

tasks {
    compileKotlin {
        dependsOn(generateSwaggerUI)
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    swaggerUI("org.webjars:swagger-ui:$swaggerUIVersion")
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
}
plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    
    // Ktor Client для OpenRouter
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")
    
    // Exposed (SQL ORM)
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.45.0")
    
    // H2 Database
    implementation("com.h2database:h2:2.2.224")
    
    // PostgreSQL (для production)
    implementation("org.postgresql:postgresql:42.7.1")
    
    // Logback для логирования
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Kotlinx Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Kotlinx DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("org.example.weather.WeatherServerKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.example.weather.WeatherServerKt"
    }
    
    // Fat JAR с зависимостями
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

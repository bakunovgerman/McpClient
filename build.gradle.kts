plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
    `maven-publish`
    signing
}

group = "io.github.bakunovgerman" // Замените на ваш GitHub username
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    
    // Ktor client for HTTP requests
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")

    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("org.example.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

// Создание source jar для публикации
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

// Создание javadoc jar для публикации
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "mcp-client"
            version = project.version.toString()
            
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            
            pom {
                name.set("MCP Client for Kotlin/JVM")
                description.set("A Kotlin/JVM client library for Model Context Protocol (MCP)")
                url.set("https://github.com/bakunovgerman/McpClient") // Замените на ваш GitHub URL
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("bakunovgerman") // Ваш GitHub username
                        name.set("German Bakunov") // Ваше имя
                        email.set("your.email@example.com") // Ваш email
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/bakunovgerman/McpClient.git")
                    developerConnection.set("scm:git:ssh://github.com/bakunovgerman/McpClient.git")
                    url.set("https://github.com/bakunovgerman/McpClient")
                }
            }
        }
    }
    
    repositories {
        // Для локального тестирования
        maven {
            name = "Local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
        
        // Для Maven Central (потребуется настройка Sonatype OSSRH)
        maven {
            name = "OSSRH"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            } else {
                uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
            credentials {
                username = project.findProperty("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

// Signing для Maven Central (опционально)
signing {
    // Только если настроены GPG ключи
    if (project.hasProperty("signing.keyId")) {
        sign(publishing.publications["maven"])
    }
}
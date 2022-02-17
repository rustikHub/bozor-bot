import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.6.10"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
}

group = "uz.rustik"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    maven {
        url = uri("http://gitlab.ugnis.uz/api/v4/projects/255/packages/maven")
        name = "GitLab"
        isAllowInsecureProtocol = true
        credentials(HttpHeaderCredentials::class) {
            name = "Private-Token"
            value = "glpat-WG89FBCm83-AW5vX91xx"
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
    mavenCentral()
}

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-noarg:1.6.10")
    }
}

apply(plugin = "org.jetbrains.kotlin.plugin.jpa")


dependencies {
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.telegram:telegrambots:5.4.0.1")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("uz.ugnis:tg-bot-lib:0.1.17")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

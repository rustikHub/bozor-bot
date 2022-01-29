import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
}

group = "uz.rustik"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    maven {
        url = uri("http://gitlab.ugnis.uz/api/v4/projects/253/packages/maven")
        name = "GitLab"
        isAllowInsecureProtocol = true
        credentials(HttpHeaderCredentials::class) {
            name = "Private-Token"
            value = System.getenv("GITLAB_TOKEN")
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
    mavenCentral()
}

dependencies {
    implementation("uz.rustik:test-lib:0.0.3-SNAPSHOT")
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

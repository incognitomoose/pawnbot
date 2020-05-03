import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.2.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.3.71"
    kotlin("plugin.spring") version "1.3.71"
    kotlin("plugin.jpa") version "1.3.71"
    id("nebula.release") version "15.0.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.13"
    id("com.github.ben-manes.versions") version "0.21.0"
}

group = "net.shelg.pawnbot"
java.sourceCompatibility = JavaVersion.VERSION_11

val stableRegex = "^[0-9,.v-]+(-r)?$".toRegex()
fun isNonStable(version: String) =
        listOf("RELEASE", "FINAL", "GA").none { version.toUpperCase().contains(it) }
                && !stableRegex.matches(version)

tasks.withType<DependencyUpdatesTask> {
    resolutionStrategy {
        componentSelection {
            all {
                if (isNonStable(candidate.version)) {
                    reject("Not a release")
                }
            }
        }
    }
}

springBoot {
    buildInfo()
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("net.dv8tion:JDA:4.1.1_142")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.codehaus.janino:janino:3.1.2")

    implementation(platform("com.google.cloud:libraries-bom:5.3.0"))
    implementation("com.google.cloud:google-cloud-texttospeech")
    implementation("com.sedmelluq:jda-nas:1.1.0")

    implementation("com.sedmelluq:lavaplayer:1.3.47")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")

    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.create("deploy") {
    doLast {
        val jarFile = tasks.named("bootJar", BootJar::class.java).get().archiveFile.get().asFile
        println("Running deploy.sh ${jarFile.parent} ${jarFile.name}")
        exec {
            commandLine("bash", "deploy.sh", jarFile.parent, jarFile.name)
        }
    }
}

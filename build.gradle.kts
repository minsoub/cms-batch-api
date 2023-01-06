import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("kapt") version "1.6.21"
}

group = "com.bithumbsystems"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17
val awssdkVersion = "2.18.10"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // Spring
    implementation("org.springframework.boot:spring-boot-configuration-processor:2.6.7")

    // Kotlin
    kapt("org.springframework.boot:spring-boot-configuration-processor:2.6.7")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.google.code.gson:gson:2.9.0")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")

    // netty M1
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.77.Final:osx-aarch_64")

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.redisson:redisson-spring-boot-starter:3.17.6")
    implementation("it.ozimov:embedded-redis:0.7.1")

    implementation("net.javacrumbs.shedlock:shedlock-provider-redis-spring:5.0.1")
    implementation("net.javacrumbs.shedlock:shedlock-spring:5.0.1")

    // mongo
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // aws
    implementation("software.amazon.awssdk:bom:$awssdkVersion")
    implementation("software.amazon.awssdk:aws-core:$awssdkVersion")
    implementation("software.amazon.awssdk:auth")
    implementation("software.amazon.awssdk:kms:$awssdkVersion")
    implementation("software.amazon.awssdk:ssm:$awssdkVersion")

    implementation("io.awspring.cloud:spring-cloud-starter-aws:2.4.1")
    implementation("io.awspring.cloud:spring-cloud-starter-aws-parameter-store-config:2.4.1")

    // test
    implementation("org.springframework.batch:spring-batch-test:4.3.7")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.amshove.kluent:kluent:1.68")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.mockk:mockk:1.12.4")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$rootDir/config/detekt.yml")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    mainClass.set("com.bithumbsystems.cms.CmsBatchApiApplicationKt")
}

tasks.jar {
    enabled = false
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Test>("test") {
    systemProperty("spring.profiles.active", "test")
}

kover {
    verify {
        onCheck.set(true)
        rule {
            isEnabled = true
            name = null
            target = kotlinx.kover.api.VerificationTarget.ALL

            bound {
                minValue = 60
                maxValue = 100
                counter = kotlinx.kover.api.CounterType.LINE
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}

import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.type
import org.gradle.internal.impldep.org.bouncycastle.asn1.x500.style.RFC4519Style.c
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
java {
    sourceSets {
        get("main").apply {
            java.srcDir("src/main/java")
            resources.srcDir("resources")
            buildDir = file("target")
            output.dir(file("$buildDir/$name"))
            java.outputDir = file("$buildDir/$name")
        }
        get("test").apply {
            java.srcDir("test/java")
            resources.srcDir("test-resources")
            buildDir = file("target")
            output.dir(file("$buildDir/$name"))
            java.outputDir = file("$buildDir/$name")
        }
    }
}

plugins {
    java
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.graalvm.buildtools.native") version "0.9.20"
    id("com.github.node-gradle.node") version "3.1.1"
}

group = "com.makezurich2023"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("io.micrometer:micrometer-registry-influx")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

node {
    version.set("18.16.0")
    npmVersion.set("9.5.1")
    download.set(true)
    distBaseUrl.set("https://nodejs.org/dist")
    workDir.set(file("$buildDir/.cache/nodejs"))
    npmWorkDir.set(file("$buildDir/.cache/npm"))
//    nodeModulesDir.set(file("$buildDir/front-src/node_modules"))
}

val buildDirPathname = "$buildDir/front"
val doInstall by tasks.register("npmDoInstall", (NpmTask::class)) {
    dependsOn(copyReactAppToTarget)
    workingDir.set((file(buildDirPathname)))
    inputs.dir("$buildDirPathname")
    inputs.file("$buildDirPathname/package.json")
    npmCommand.set(listOf("install"))
}

val bundle by tasks.register("npmBuild", (NpmTask::class)) {
    dependsOn(doInstall)
    workingDir.set((file(buildDirPathname)))
    inputs.dir("$buildDirPathname")
    inputs.dir("$buildDirPathname/node_modules")
    inputs.dir("$buildDirPathname/node_modules/.bin")
    inputs.file("$buildDirPathname/package.json")
    outputs.dir("$buildDir/resources/main/front")
    args.set(kotlin.collections.listOf("run", "build"))
}

val copyReactAppToTarget by tasks.register("copyReactAppToTarget", (Copy::class)) {
    from("src/main/front")
    exclude("node_modules")
    into("$buildDir/front")
}


val copyReactAppToTargetResources by tasks.register("copyReactAppToTargetResources", (Copy::class)) {
    dependsOn(bundle)
    from("$buildDir/front/dist")
    into("$buildDir/resources/main/front")
}
tasks.getByName("bootJar") {
    dependsOn(copyReactAppToTargetResources)
}
tasks.getByName("compileAotJava") {
    dependsOn(copyReactAppToTargetResources)
}



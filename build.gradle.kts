plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.freefair.lombok") version "6.0.0-m2"
    id("org.hibernate.build.maven-repo-auth") version "3.0.4"
}

subprojects {
    group = "pro.husk"
    version = "2.1.0-SNAPSHOT"

    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "io.freefair.lombok")
    apply(plugin= "org.hibernate.build.maven-repo-auth")

    repositories {
        mavenLocal()
        mavenCentral()

        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }

        maven {
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }

        maven {
            url = uri("https://repo.aikar.co/content/groups/aikar/")
        }

        maven {
            url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
        }

        maven {
            url = uri("https://maven.husk.pro/snapshots/")
        }

        maven {
            url = uri("https://maven.enginehub.org/repo/")
        }
    }

    dependencies {
        implementation("co.aikar:taskchain-bukkit:3.7.2")
        compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0")
        compileOnly("net.md-5:bungeecord-chat:1.16-R0.4")
    }

    tasks {
        withType<JavaCompile>() {
            options.encoding = "UTF-8"
        }

        build {
            dependsOn(shadowJar)
        }
    }
}
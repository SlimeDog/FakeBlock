description = "FakeBlock-intermediate"
java.sourceCompatibility = JavaVersion.VERSION_1_8

dependencies {
    implementation(project(":core"))
    implementation(project(":common"))
    compileOnly("org.spigotmc:spigot-api:1.16.1-R0.1-SNAPSHOT")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set(project.description + "-" + project.version + ".jar")
    relocate("co.aikar.taskchain", "pro.husk.fakeblock.taskchain")
    relocate("co.aikar.commands", "pro.husk.fakeblock.acf")
    relocate("co.aikar.locales", "pro.husk.fakeblock.acf.locales")
}

publishing {
    repositories {
        maven {
            name = "husk"
            val baseUrl = "https://maven.husk.pro/"
            val releasesRepoUrl = baseUrl + "releases"
            val snapshotsRepoUrl = baseUrl + "snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = description
            version = version
            from(components["java"])
        }
    }
}
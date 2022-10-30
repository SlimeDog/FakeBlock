description = "FakeBlock-latest"
java.sourceCompatibility = JavaVersion.VERSION_11

dependencies {
    implementation(project(":core"))
    implementation(project(":common"))
    compileOnly("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.20")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.1-SNAPSHOT")
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
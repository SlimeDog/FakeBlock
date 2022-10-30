description = "FakeBlock-common"
java.sourceCompatibility = JavaVersion.VERSION_1_8

dependencies {
    implementation(project(":core"))
    implementation("net.jodah:expiringmap:0.5.9")
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
    implementation("pro.husk:ConfigAnnotations:1.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.13.2-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.2")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.0-SNAPSHOT")
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
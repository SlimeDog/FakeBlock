description = "FakeBlock-legacy"
java.sourceCompatibility = JavaVersion.VERSION_1_8

dependencies {
    implementation(project(":core"))
    implementation(project(":common"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set(project.description + "-" + project.version + ".jar")
    relocate("co.aikar.taskchain", "pro.husk.fakeblock.taskchain")
    relocate("co.aikar.commands", "pro.husk.fakeblock.acf")
    relocate("co.aikar.locales", "pro.husk.fakeblock.acf.locales")
}

import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java")
    id("eclipse")
}

val pluginGroup: String by project
val pluginVersion: String by project

group = pluginGroup
version = pluginVersion

java {
    sourceCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

repositories {
    mavenCentral()
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    implementation("com.googlecode.json-simple:json-simple:1.1")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().resources.srcDirs) {
        filter<ReplaceTokens>("tokens" to mapOf("version" to project.version))
    }
}


plugins {
    id("mod-platform")
    id("maven-publish")
    id("net.neoforged.moddev")
}

stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)
}

platform {
    loader = "neoforge"
    dependencies {
        required("minecraft") {
            forgeLikeVersionRange = prop("deps.minecraft")
        }
        required("neoforge") {
            forgeLikeVersionRange.set("[1,)")
        }
    }
}

neoForge {
    version = prop("deps.neoforge")
    accessTransformers.from(rootProject.file("src/main/resources/aw/${stonecutter.current.version}.cfg"))
    validateAccessTransformers = true

    if (hasProperty("deps.parchment")) parchment {
        val (mc, ver) = prop("deps.parchment").split(':')
        mappingsVersion = ver
        minecraftVersion = mc
    }

    runs {
        register("client") {
            client()
            gameDirectory = file("run/")
            ideName = "NeoForge Client (${stonecutter.current.version})"
            programArgument("--username=Dev")
        }
        register("server") {
            server()
            gameDirectory = file("run/")
            ideName = "NeoForge Server (${stonecutter.current.version})"
        }
    }

    mods {
        register(prop("mod.id")) {
            sourceSet(sourceSets["main"])
        }
    }
    sourceSets["main"].resources.srcDir("${rootDir}/versions/datagen/${sc.current.version.split("-")[0]}/src/main/generated")
}

repositories {
    mavenCentral()
    strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }

    exclusiveContent {
        forRepository {
            maven {
                url = uri("https://cursemaven.com")
            }
        }
        filter {
            includeGroup("curse.maven")
        }
    }

    maven {
        name = "sshcrackRepositoryReleases"
        url = uri("https://maven.sshcrack.me/releases")
    }

    maven {
        name = "LDTTeam - Mods Maven"
        url = uri("https://ldtteam.jfrog.io/ldtteam/mods-maven/")
    }

    maven {
        name = "Jared's Maven"
        url = uri("https://maven.blamejared.com/")
    }

    maven("https://maven.isxander.dev/releases") {
        name = "Xander Maven"
    }

    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
}



dependencies {
    implementation(libs.moulberry.mixinconstraints)
    jarJar(libs.moulberry.mixinconstraints)

    implementation("me.sshcrack:mc_talking:${prop("deps.talking_colonists_version")}-${prop("deps.minecraft")}-neoforge")

    implementation("com.ldtteam:minecolonies:${prop("deps.minecolonies")}")
    implementation("me.sshcrack:gemini_live_lib:${prop("deps.gemini_live_lib")}-${prop("deps.minecraft")}-neoforge")

    runtimeOnly("com.ldtteam:domum-ornamentum:${prop("deps.domum")}")
    runtimeOnly("com.ldtteam:structurize:${prop("deps.structurize")}")
    runtimeOnly("com.ldtteam:blockui:${prop("deps.blockui")}")

    runtimeOnly("maven.modrinth:simple-voice-chat:neoforge-${prop("deps.minecraft")}-${prop("deps.voice_chat")}")
    implementation("dev.isxander:yet-another-config-lib:${prop("deps.yacl")}+${prop("deps.minecraft")}-neoforge")
}

var loader = sc.current.component1().split("-")[1]
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = prop("mod.group")
            artifactId = prop("mod.id")
            version = "${prop("mod.version")}-${prop("deps.minecraft")}-${loader}"

            artifact(tasks.named("jar"))
            tasks.findByName("sourcesJar")?.let { artifact(it) }
        }
    }

repositories {
        maven {
            name = "sshcrack"
            url = uri("https://maven.sshcrack.me/releases")

            credentials {
                username = (findProperty("sshcrackRepoMavenUser") as String?)
                    ?: System.getenv("sshcrackRepoMavenUser")
                password = (findProperty("sshcrackRepoMavenPassword") as String?)
                    ?: System.getenv("sshcrackRepoMavenPassword")
            }
        }
    }
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
}

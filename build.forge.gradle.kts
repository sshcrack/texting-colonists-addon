plugins {
    id("mod-platform")
    id("maven-publish")
    id("net.neoforged.moddev.legacyforge")
}

stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)
}

platform {
    loader = "forge"
    dependencies {
        required("minecraft") {
            forgeLikeVersionRange = prop("deps.minecraft")
        }
        required("forge") {
            forgeLikeVersionRange.set("[1,)")
        }
    }
}

legacyForge {
    version = "${prop("deps.minecraft")}-${prop("deps.forge")}"

    validateAccessTransformers = true

    accessTransformers.from(
        rootProject.file("src/main/resources/aw/${sc.current.version}.cfg")
    )

    runs {
        register("client") {
            client()
            gameDirectory = file("run/")
            ideName = "Forge Client (${sc.current.version})"
            programArgument("--username=Dev")
        }
        register("server") {
            server()
            gameDirectory = file("run/")
            ideName = "Forge Server (${sc.current.version})"
        }
    }

    mods {
        register(prop("mod.id")) {
            sourceSet(sourceSets["main"])
        }
    }
}

mixin {
    add(sourceSets.main.get(), "${prop("mod.id")}.mixins.refmap.json")
    config("${prop("mod.id")}.mixins.json")
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
    annotationProcessor("org.spongepowered:mixin:${libs.versions.mixin.get()}:processor")

    implementation(libs.moulberry.mixinconstraints)
    jarJar(libs.moulberry.mixinconstraints)

    modImplementation("me.sshcrack:mc_talking:${prop("deps.talking_colonists_version")}-${prop("deps.minecraft")}-forge")

    modImplementation("com.ldtteam:minecolonies:${prop("deps.minecolonies")}")
    modImplementation("me.sshcrack:gemini_live_lib:${prop("deps.gemini_live_lib")}-${prop("deps.minecraft")}-forge")

    modRuntimeOnly("com.ldtteam:domum_ornamentum:${prop("deps.domum")}:universal")
    modRuntimeOnly("com.ldtteam:structurize:${prop("deps.structurize")}")
    modRuntimeOnly("com.ldtteam:blockui:${prop("deps.blockui")}")

    modRuntimeOnly("maven.modrinth:simple-voice-chat:forge-${prop("deps.minecraft")}-${prop("deps.voice_chat")}")
    modImplementation("dev.isxander:yet-another-config-lib:${prop("deps.yacl")}+${prop("deps.minecraft")}-forge")
}

sourceSets {
    main {
        resources.srcDir(
            "${rootDir}/versions/datagen/${sc.current.version.split("-")[0]}/src/main/generated"
        )
    }
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

# Template Mod

A multi-loader Minecraft mod targeting Forge 1.20.1 and NeoForge 1.21.1, built with [Stonecutter](https://github.com/kikugie/stonecutter).

## Getting Started

Run `./init.sh` to customize the template with your mod's details.

## Development

```bash
./gradlew build
```

To run a specific version:
```bash
./gradlew :1.20.1-forge:runClient
./gradlew :1.21.1-neoforge:runClient
```

## Structure

- `src/main/java` — Shared main source code with Stonecutter conditional comments
- `build.forge.gradle.kts` — Forge-specific build configuration (1.20.1)
- `build.neoforge.gradle.kts` — NeoForge-specific build configuration (1.21.1)
- `build-logic/` — Custom Gradle plugin for mod manifest generation and publishing

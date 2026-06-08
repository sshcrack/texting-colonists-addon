#!/usr/bin/env bash
set -euo pipefail

# ──────────────────────────────────────────────
# Multi-Loader Minecraft Mod Template Initializer
# ──────────────────────────────────────────────

SELF="$(realpath "$0")"
ROOT="$(dirname "$SELF")"

# --- helpers ----------------------------------------------------------

sed_inplace() {
  local file="$1" expr="$2"
  if [[ "$(uname -s)" == Darwin* ]]; then
    sed -i '' "$expr" "$file"
  else
    sed -i "$expr" "$file"
  fi
}

prompt() {
  local var="$1" label="$2" default="$3"
  local val
  if [[ -n "$default" ]]; then
    read -r -p "$label [$default]: " val
    echo "${val:-$default}"
  else
    read -r -p "$label: " val
    echo "${val:-}"
  fi
}

slugify() {
  echo "$1" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9_.-]/_/g'
}

die() {
  echo "error: $*" >&2
  exit 1
}

# --- collect answers --------------------------------------------------

echo ""
echo "╔═══════════════════════════════════════════════════╗"
echo "║  Minecraft Multi-Loader Mod Template Initializer ║"
echo "╚═══════════════════════════════════════════════════╝"
echo ""

MOD_ID=$(prompt "mod_id" "Mod ID (slug, e.g. my_mod)" "template")
MOD_ID=$(slugify "$MOD_ID")

PKG=$(prompt "pkg" "Package name (e.g. com.example.my_mod)" "dev.placeholder.modid")
MOD_NAME=$(prompt "mod_name" "Mod display name" "Template Mod")
MOD_VERSION=$(prompt "mod_version" "Mod version" "1.0.0")
AUTHORS=$(prompt "authors" "Author(s) (comma-separated)" "Your Name")
DESCRIPTION=$(prompt "description" "Short description" "A Minecraft mod")
LICENSE_NAME=$(prompt "license_name" "License name" "All rights reserved")
LICENSE_URL=$(prompt "license_url" "License URL" "https://example.com/license")
SOURCES_URL=$(prompt "sources_url" "Source code URL" "https://github.com/your-org/your-mod")
HOMEPAGE_URL=$(prompt "homepage_url" "Homepage URL" "https://github.com/your-org/your-mod")
ISSUES_URL=$(prompt "issues_url" "Issues URL" "https://github.com/your-org/your-mod/issues")
DEVELOPER_ID=$(prompt "developer_id" "Developer ID for POM" "your-name")
DEVELOPER_NAME=$(prompt "developer_name" "Developer name for POM" "Your Name")
DEVELOPER_URL=$(prompt "developer_url" "Developer URL for POM" "https://github.com/your-org")

echo ""
echo "═══════════════════════════════════════════════════"
echo " Initializing template with:"
echo "  Mod ID:      $MOD_ID"
echo "  Package:     $PKG"
echo "  Name:        $MOD_NAME"
echo "  Version:     $MOD_VERSION"
echo "  Author(s):   $AUTHORS"
echo "═══════════════════════════════════════════════════"
echo ""

# --- 1. Update stonecutter.properties.toml ----------------------------

PROPS="$ROOT/stonecutter.properties.toml"
echo "  • Updating $PROPS"

sed_inplace "$PROPS" "s/^mod\.id = \".*\"/mod.id = \"$MOD_ID\"/"
sed_inplace "$PROPS" "s/^mod\.name = \".*\"/mod.name = \"$MOD_NAME\"/"
sed_inplace "$PROPS" "s|^mod\.group = \".*\"|mod.group = \"$PKG\"|"
sed_inplace "$PROPS" "s/^mod\.version = \".*\"/mod.version = \"$MOD_VERSION\"/"
sed_inplace "$PROPS" "s/^mod\.description = \".*\"/mod.description = \"$DESCRIPTION\"/"
sed_inplace "$PROPS" "s/^mod\.authors = \[.*\]/mod.authors = [\"${AUTHORS/, /\", \"}\"]/"
sed_inplace "$PROPS" "s|^mod\.license\.name = \".*\"|mod.license.name = \"$LICENSE_NAME\"|"
sed_inplace "$PROPS" "s|^mod\.license\.url = \".*\"|mod.license.url = \"$LICENSE_URL\"|"
sed_inplace "$PROPS" "s|^mod\.sources_url = \".*\"|mod.sources_url = \"$SOURCES_URL\"|"
sed_inplace "$PROPS" "s|^mod\.homepage_url = \".*\"|mod.homepage_url = \"$HOMEPAGE_URL\"|"
sed_inplace "$PROPS" "s|^mod\.issues_url = \".*\"|mod.issues_url = \"$ISSUES_URL\"|"

# Update POM developers section
sed_inplace "$PROPS" "/^\[\[mod\.pom\.developers\]\]/,/^\[/ s/^id = \".*\"/id = \"$DEVELOPER_ID\"/"
sed_inplace "$PROPS" "/^\[\[mod\.pom\.developers\]\]/,/^\[/ s/^name = \".*\"/name = \"$DEVELOPER_NAME\"/"
sed_inplace "$PROPS" "/^\[\[mod\.pom\.developers\]\]/,/^\[/ s|^url = \".*\"|url = \"$DEVELOPER_URL\"|"

# --- 2. Rename Java package directory --------------------------------

OLD_PKG_DIR="dev/placeholder/modid"
NEW_PKG_DIR="${PKG//./\/}"

OLD_PKG_BASE="dev.placeholder"
OLD_PKG_LEAF="modid"

if [[ "$OLD_PKG_DIR" != "$NEW_PKG_DIR" ]]; then
  # Find the common prefix to know where to start
  OLD_SRC="$ROOT/src/main/java/$OLD_PKG_DIR"
  NEW_SRC="$ROOT/src/main/java/$NEW_PKG_DIR"
  PARENT="$(dirname "$NEW_SRC")"

  echo "  • Renaming package: $OLD_PKG_DIR → $NEW_PKG_DIR"
  mkdir -p "$PARENT"
  mv "$OLD_SRC" "$NEW_SRC" 2>/dev/null || die "Failed to rename package directory"

  # Remove any leftover empty directories from the old package path
  pushd "$ROOT/src/main/java" >/dev/null
  OLD_DIR="$OLD_PKG_DIR"
  while [[ "$OLD_DIR" != "." ]] && [[ "$OLD_DIR" != "/" ]]; do
    PARENT_DIR="$(dirname "$OLD_DIR")"
    rmdir "$OLD_DIR" 2>/dev/null || true
    OLD_DIR="$PARENT_DIR"
  done
  popd >/dev/null

  # Update package declarations in all Java files
  echo "  • Updating package declarations in Java files"
  find "$ROOT/src/main/java" -name '*.java' -exec sed_inplace {} "s/^package $OLD_PKG_BASE\.$OLD_PKG_LEAF/package $PKG/" \;
  find "$ROOT/src/main/java" -name '*.java' -exec sed_inplace {} "s/^package $OLD_PKG_BASE\.$OLD_PKG_LEAF\./package $PKG./" \;
fi

# --- 3. Rename mixins.json -------------------------------------------

OLD_MIXINS="$ROOT/src/main/resources/template.mixins.json"

# Rename mixins.json to match mod id
if [[ -f "$OLD_MIXINS" ]] && [[ "template" != "$MOD_ID" ]]; then
  NEW_MIXINS="$ROOT/src/main/resources/${MOD_ID}.mixins.json"
  echo "  • Renaming mixins config: template.mixins.json → ${MOD_ID}.mixins.json"
  mv "$OLD_MIXINS" "$NEW_MIXINS"
fi

# Update package reference inside mixins.json
MIXINS_FILE="$ROOT/src/main/resources/${MOD_ID}.mixins.json"
if [[ -f "$MIXINS_FILE" ]]; then
  sed_inplace "$MIXINS_FILE" "s|\"package\": \".*\"|\"package\": \"${PKG}.mixin\"|"
fi

# --- 4. Update pack.mcmeta -------------------------------------------

MCMETA="$ROOT/src/main/resources/pack.mcmeta"
if [[ -f "$MCMETA" ]]; then
  echo "  • Updating pack.mcmeta"
  sed_inplace "$MCMETA" "s|\"description\": \".*\"|\"description\": \"${MOD_NAME} resources\"|"
fi

# --- 5. Update README.md ---------------------------------------------

README="$ROOT/README.md"
if [[ -f "$README" ]]; then
  echo "  • Updating README.md"
  cat > "$README" <<- README_EOF
	# $MOD_NAME

	$DESCRIPTION

	Built with the multi-loader template targeting Forge 1.20.1 and NeoForge 1.21.1.

	## Development

	\`\`\`bash
	./gradlew build
	\`\`\`

	\`\`\`bash
	./gradlew :1.20.1-forge:runClient
	./gradlew :1.21.1-neoforge:runClient
	\`\`\`

	## License

	$LICENSE_NAME
	README_EOF
fi

# --- done -------------------------------------------------------------

echo ""
echo "✔  Template initialized successfully!"
echo ""
echo "Next steps:"
echo "  1. Regenerate IDE files if needed"
echo "  2. Run ./gradlew build to verify everything works"
echo ""

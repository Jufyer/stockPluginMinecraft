#!/bin/bash

PLUGIN_NAME="YourPlugin.jar"
SERVERS_FOLDER="servers"

# Function to show main menu
show_menu() {
    clear
    echo "=============================="
    echo "   Minecraft Multi-Version Server Manager"
    echo "=============================="
    echo
    echo "[1] Neuen Server erstellen (Paper / Purpur / Folia)"
    echo "[2] Plugin builden + in alle Server kopieren"
    echo "[3] Server starten"
    echo "[4] Beenden"
    echo
    read -p "Auswahl: " CHOICE
}

# Function to create a new server
create_server() {
    clear
    echo "=============================="
    echo "        Neuen Server erstellen"
    echo "=============================="
    echo
    read -p "Loader (paper / purpur / folia): " LOADER
    read -p "Minecraft Version (z.B. 1.21.3): " VERSION

    TARGET="$SERVERS_FOLDER/$LOADER-$VERSION"
    mkdir -p "$TARGET/plugins"

    echo "Lade server.jar herunter..."
    if [[ "$LOADER" == "paper" ]]; then
        curl -L -o "$TARGET/server.jar" "https://api.papermc.io/v2/projects/paper/versions/$VERSION/builds/1/downloads/paper-$VERSION-1.jar"
    elif [[ "$LOADER" == "folia" ]]; then
        curl -L -o "$TARGET/server.jar" "https://api.papermc.io/v2/projects/folia/versions/$VERSION/builds/1/downloads/folia-$VERSION-1.jar"
    elif [[ "$LOADER" == "purpur" ]]; then
        curl -L -o "$TARGET/server.jar" "https://api.purpurmc.org/v2/purpur/$VERSION/latest/download"
    else
        echo "Ungültiger Loader."
        read -p "Drücke Enter um zurückzukehren..." && return
    fi

    echo "EULA akzeptieren..."
    cat > "$TARGET/eula.txt" <<EOF
#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).
#$(date -u "+%a %b %d %H:%M:%S UTC %Y")
eula=true
EOF

    echo "Erstelle start.sh..."
    cat > "$TARGET/start.sh" <<EOF
#!/bin/bash
java -Xms1G -Xmx2G -jar server.jar nogui
EOF
    chmod +x "$TARGET/start.sh"

    echo
    echo "Server erfolgreich erstellt:"
    echo "$TARGET"
    echo
    read -p "Drücke Enter um zurückzukehren..." && return
}

# Function to build plugin and copy to all servers
build_and_copy() {
    clear
    echo "=============================="
    echo "     Plugin builden und verteilen"
    echo "=============================="
    echo

    if ! ./gradlew build; then
        echo "Build fehlgeschlagen!"
        read -p "Drücke Enter um zurückzukehren..." && return
    fi

    BUILD_JAR=$(find build/libs -name "*.jar" | head -n1)
    if [[ -z "$BUILD_JAR" ]]; then
        echo "Keine Build-Datei gefunden."
        read -p "Drücke Enter um zurückzukehren..." && return
    fi

    echo "Benutze Build-Datei: $BUILD_JAR"

    for server_dir in "$SERVERS_FOLDER"/*/; do
        if [[ -d "$server_dir" ]]; then
            echo "Kopiere nach $server_dir/plugins/"
            cp "$BUILD_JAR" "$server_dir/plugins/$PLUGIN_NAME"
        fi
    done

    echo
    echo "Fertig!"
    read -p "Drücke Enter um zurückzukehren..." && return
}

# Function to start a server
start_server() {
    clear
    echo "=============================="
    echo "          Server starten"
    echo "=============================="
    echo

    i=0
    for server_dir in "$SERVERS_FOLDER"/*/; do
        if [[ -d "$server_dir" ]]; then
            i=$((i + 1))
            srv[$i]="$server_dir"
            echo "$i. ${server_dir%/}"
        fi
    done

    echo
    read -p "Nummer eingeben: " NR

    if [[ -z "${srv[$NR]}" ]]; then
        echo "Ungültige Auswahl."
        read -p "Drücke Enter um zurückzukehren..." && return
    fi

    SELECTED="${srv[$NR]}"
    echo "Starte: $SELECTED"
    cd "$SELECTED"
    gnome-terminal -- bash -c "./start.sh; exec bash" &
    cd ../..
    read -p "Drücke Enter um zurückzukehren..." && return
}

# Main loop
while true; do
    show_menu
    case "$CHOICE" in
        1) create_server ;;
        2) build_and_copy ;;
        3) start_server ;;
        4) exit 0 ;;
        *) echo "Ungültige Auswahl." ;;
    esac
done
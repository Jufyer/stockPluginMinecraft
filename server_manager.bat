@echo off
setlocal enabledelayedexpansion

set PLUGIN_NAME=YourPlugin.jar
set SERVERS_FOLDER=servers

:: =============================================
:: MAIN MENU
:: =============================================

:menu
cls
echo =============================================
echo      Minecraft Multi-Version Server Manager
echo =============================================
echo.
echo [1] Neuen Server erstellen (Paper / Purpur / Folia)
echo [2] Plugin builden + in alle Server kopieren
echo [3] Server starten
echo [4] Beenden
echo.
set /p CHOICE="Auswahl: "

if "%CHOICE%"=="1" goto create_server
if "%CHOICE%"=="2" goto build_and_copy
if "%CHOICE%"=="3" goto start_server
if "%CHOICE%"=="4" exit
goto menu

:: =============================================
:: 1) CREATE NEW SERVER
:: =============================================

:create_server
cls
echo =============================================
echo           Neuen Server erstellen
echo =============================================
echo.
set /p LOADER="Loader (paper / purpur / folia): "
set /p VERSION="Minecraft Version (z.B. 1.21.3): "

set TARGET=%SERVERS_FOLDER%\%LOADER%-%VERSION%
mkdir "%TARGET%" >nul 2>&1
mkdir "%TARGET%\plugins" >nul 2>&1

echo.
echo Lade server.jar herunter...
if /i "%LOADER%"=="paper" (
    curl -L -o "%TARGET%\server.jar" "https://api.papermc.io/v2/projects/paper/versions/%VERSION%/builds/1/downloads/paper-%VERSION%-1.jar"
)
if /i "%LOADER%"=="folia" (
    curl -L -o "%TARGET%\server.jar" "https://api.papermc.io/v2/projects/folia/versions/%VERSION%/builds/1/downloads/folia-%VERSION%-1.jar"
)
if /i "%LOADER%"=="purpur" (
    curl -L -o "%TARGET%\server.jar" "https://api.purpurmc.org/v2/purpur/%VERSION%/latest/download"
)

echo.
echo EULA akzeptieren...
(
    echo #By changing the setting below to TRUE you are indicating your agreement to our EULA ^(https://aka.ms/MinecraftEULA^).
    echo #Wed Nov 19 00:22:01 CET 2025
    echo eula=true
) > "%TARGET%\eula.txt"

echo.
echo Erstelle start.bat...
(
echo @echo off
echo java -Xms1G -Xmx2G -jar server.jar nogui
) > "%TARGET%\start.bat"

echo.
echo Server erfolgreich erstellt:
echo %TARGET%
echo.
pause
goto menu

:: =============================================
:: 2) Build Plugin + Copy to all Servers
:: =============================================

:build_and_copy
cls
echo =============================================
echo      Plugin builden und verteilen
echo =============================================
echo.

call gradlew build || (
    echo Build fehlgeschlagen!
    pause
    goto menu
)

for %%f in (build\libs\*.jar) do set BUILD_JAR=%%f
echo Benutze Build-Datei: %BUILD_JAR%

for /d %%d in (%SERVERS_FOLDER%\*) do (
    echo Kopiere nach %%d\plugins\
    copy "%BUILD_JAR%" "%%d\plugins\%PLUGIN_NAME%" /Y >nul
)

echo.
echo Fertig!
pause
goto menu

:: =============================================
:: 3) Start Server
:: =============================================

:start_server
cls
echo =============================================
echo          Server starten
echo =============================================
echo.

set i=0
for /d %%d in (%SERVERS_FOLDER%\*) do (
    set /a i+=1
    set "srv[!i!]=%%d"
    echo !i!. %%~nd
)

echo.
set /p NR="Nummer eingeben: "
set SELECTED=!srv[%NR%]!

if "%SELECTED%"=="" (
    echo Ungueltige Auswahl.
    pause
    goto menu
)

echo Starte: %SELECTED%
cd "%SELECTED%"
start cmd /k "start.bat"
cd ../../
pause
goto menu

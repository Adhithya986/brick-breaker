@echo off
echo ========================================================
echo         Launching Brick Breaker Game
echo ========================================================

java --enable-native-access=ALL-UNNAMED -cp "lib/*;bin" com.brickbreaker.Main

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] An error occurred while running Brick Breaker.
    pause
)

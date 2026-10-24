@echo off
echo ========================================================
echo         Building Brick Breaker Game (Java Swing)
echo ========================================================

if not exist bin mkdir bin

echo Compiling Java source files...
javac --release 21 -cp "lib/*;src" -d bin src\com\brickbreaker\*.java src\com\brickbreaker\model\*.java src\com\brickbreaker\db\*.java src\com\brickbreaker\ui\*.java src\com\brickbreaker\audio\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Compilation completed successfully!
echo Run the game using 'run.bat'

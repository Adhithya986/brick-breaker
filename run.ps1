Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "         Launching Brick Breaker Game" -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan

java --enable-native-access=ALL-UNNAMED -cp "lib/*;bin" com.brickbreaker.Main

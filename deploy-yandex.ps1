# TourOS Yandex WebApp Hızlı Deploy Scripti
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TourOS Web App Build Baslatiliyor...  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Gradle WasmJS Build
.\gradlew.bat :webApp:wasmJsBrowserDistribution --no-daemon

if ($LASTEXITCODE -ne 0) {
    Write-Host "[HATA] WebApp build basarisiz oldu!" -ForegroundColor Red
    exit 1
}

Write-Host "`n[1/3] Build basariyla tamamlandi." -ForegroundColor Green

# 2. Sunucuya Aktarma
$serverIp = "81.26.178.103"
$sshKey = "$env:USERPROFILE\.ssh\touros_clean_key"
$remotePath = "/var/www/axileto.com"
$localPath = "webApp\build\dist\wasmJs\productionExecutable\*"

Write-Host "[2/3] Dosyalar Yandex sunucusuna aktariliyor ($serverIp)..." -ForegroundColor Yellow

scp -o StrictHostKeyChecking=no -i "$sshKey" -r $localPath "ubuntu@${serverIp}:${remotePath}/"

if ($LASTEXITCODE -ne 0) {
    Write-Host "[HATA] Dosya aktarimi sirasinda hata olustu!" -ForegroundColor Red
    exit 1
}

# 3. Izinleri Guncelleme
Write-Host "[3/3] Sunucu izinleri ayarlaniyor..." -ForegroundColor Yellow
ssh -o StrictHostKeyChecking=no -i "$sshKey" "ubuntu@$serverIp" "sudo chmod -R 755 $remotePath"

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  DEPLOY BASARIYLA TAMAMLANDI!          " -ForegroundColor Green
Write-Host "  URL: https://axileto.com              " -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

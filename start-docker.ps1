# ===================================================
# Quick Start Script for Windows (PowerShell)
# ===================================================

Write-Host "🐳 Starting CRM Docker Environment..." -ForegroundColor Cyan

# Check if Docker is running
Write-Host "`n1. Checking Docker..." -ForegroundColor Yellow
$dockerRunning = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker is not running! Please start Docker Desktop." -ForegroundColor Red
    exit 1
}
Write-Host "✅ Docker is running" -ForegroundColor Green

# Check if docker-compose.yml exists
if (-not (Test-Path "docker-compose.yml")) {
    Write-Host "❌ docker-compose.yml not found!" -ForegroundColor Red
    exit 1
}

# Stop and remove old containers
Write-Host "`n2. Cleaning up old containers..." -ForegroundColor Yellow
docker-compose down -v 2>&1 | Out-Null
Write-Host "✅ Cleanup complete" -ForegroundColor Green

# Build and start containers
Write-Host "`n3. Building and starting containers..." -ForegroundColor Yellow
docker-compose up -d --build

# Wait for containers to be healthy
Write-Host "`n4. Waiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

$maxAttempts = 30
$attempt = 0
$healthy = $false

while ($attempt -lt $maxAttempts -and -not $healthy) {
    $attempt++
    Write-Host "   Attempt $attempt/$maxAttempts..." -ForegroundColor Gray

    $mysqlHealth = docker inspect crm-mysql --format='{{.State.Health.Status}}' 2>&1
    $appHealth = docker inspect crm-app --format='{{.State.Health.Status}}' 2>&1

    if ($mysqlHealth -eq "healthy" -and $appHealth -eq "healthy") {
        $healthy = $true
        Write-Host "✅ All services are healthy!" -ForegroundColor Green
    } else {
        Start-Sleep -Seconds 2
    }
}

if (-not $healthy) {
    Write-Host "⚠️  Services are taking longer than expected" -ForegroundColor Yellow
    Write-Host "   Check logs: docker-compose logs -f" -ForegroundColor Gray
}

# Show container status
Write-Host "`n5. Container Status:" -ForegroundColor Yellow
docker-compose ps

# Test API
Write-Host "`n6. Testing API..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ API is responding!" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  API not ready yet. Run: docker-compose logs -f app" -ForegroundColor Yellow
}

# Summary
Write-Host "`n" + "="*50 -ForegroundColor Cyan
Write-Host "🚀 CRM System is starting!" -ForegroundColor Green
Write-Host "="*50 -ForegroundColor Cyan
Write-Host ""
Write-Host "📝 Access Points:" -ForegroundColor Yellow
Write-Host "   • API:          http://localhost:8080" -ForegroundColor White
Write-Host "   • Health Check: http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "   • MySQL:        localhost:3306" -ForegroundColor White
Write-Host ""
Write-Host "🔑 Test Credentials:" -ForegroundColor Yellow
Write-Host "   • Admin:   admin/admin123" -ForegroundColor White
Write-Host "   • Manager: manager/manager123" -ForegroundColor White
Write-Host "   • User:    user/user123" -ForegroundColor White
Write-Host ""
Write-Host "📊 Useful Commands:" -ForegroundColor Yellow
Write-Host "   • View logs:        docker-compose logs -f" -ForegroundColor Gray
Write-Host "   • Stop services:    docker-compose down" -ForegroundColor Gray
Write-Host "   • Restart services: docker-compose restart" -ForegroundColor Gray
Write-Host "   • MySQL shell:      docker exec -it crm-mysql mysql -u crm_user -pcrm_password crm_db" -ForegroundColor Gray
Write-Host ""
Write-Host "✨ Happy coding!" -ForegroundColor Cyan
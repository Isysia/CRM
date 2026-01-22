Write-Host "Rozpoczynam aktualizacje projektu..." -ForegroundColor Cyan

# 1. Збірка Backend (Java -> JAR)
Write-Host "Kompilacja Java (Maven)..." -ForegroundColor Yellow
./mvnw clean package -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Error "Blad kompilacji Maven"; exit }

# 2. Збірка Docker-образу Backend
Write-Host "Aktualizacja obrazu Backend..." -ForegroundColor Yellow
docker build -t crm-app:latest .

# 3. Збірка Docker-образу Frontend
Write-Host "Aktualizacja obrazu Frontend..." -ForegroundColor Yellow
# Переходимо в папку фронтенду
cd frontend
docker build -f Dockerfile -t crm-frontend-dev:latest .
cd ..

# 4. Перезапуск Pods у Kubernetes
Write-Host "Restart kontenerow w Kubernetes..." -ForegroundColor Yellow
kubectl rollout restart deployment crm-app -n crm
kubectl rollout restart deployment crm-frontend -n crm

Write-Host "Oczekiwanie na uruchomienie zaktualizowanych wersji..." -ForegroundColor Gray
kubectl wait --for=condition=available --timeout=120s deployment/crm-app -n crm
kubectl wait --for=condition=available --timeout=60s deployment/crm-frontend -n crm

Write-Host "`nAktualizacja zakonczona! Odswiez strone http://crm.local" -ForegroundColor Green
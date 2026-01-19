Write-Host "Uruchomienie systemu CRM w Kubernetes..." -ForegroundColor Cyan

# 1. Створення простору імен (Namespace), якщо його немає
$ns = kubectl get namespace crm -o name 2>$null
if ($null -eq $ns) {
    Write-Host "Tworzenie przestrzeni nazw 'crm'..." -ForegroundColor Yellow
    kubectl create namespace crm
}

# 2. Застосування конфігурацій та секретів
# Додаємо прапор -n crm для надійності, хоча він є у ваших YAML файлах
kubectl apply -f k8s/crm-mysql/mysql-secret.yaml -n crm
kubectl apply -f k8s/crm-mysql/mysql-pvc.yaml -n crm
kubectl apply -f k8s/crm-app/app-configmap.yaml -n crm
kubectl apply -f k8s/crm-app/app-secret.yaml -n crm

# 3. Запуск бази даних
Write-Host "Rozpoczynanie wdrazania bazy danych..." -ForegroundColor Gray
kubectl apply -f k8s/crm-mysql/mysql-deployment.yaml -n crm
kubectl apply -f k8s/crm-mysql/mysql-service.yaml -n crm

Write-Host "Oczekiwanie na gotowosc MySQL..." -ForegroundColor Gray
kubectl wait --for=condition=available --timeout=60s deployment/mysql -n crm

# 4. Запуск Backend та Frontend
kubectl apply -f k8s/crm-app/app-deployment.yaml -n crm
kubectl apply -f k8s/crm-app/app-service.yaml -n crm

Write-Host "`nBackend inicjuje baze i Spring... Czekamy na sygnal gotowosci." -ForegroundColor Gray
kubectl wait --for=condition=available --timeout=90s deployment/crm-app -n crm

kubectl apply -f k8s/crm-frontend/crm-frontend-deployment.yaml -n crm
kubectl apply -f k8s/crm-frontend/crm-frontend-service.yaml -n crm

Write-Host "`nFrontend uruchamia sie. Czekamy na sygnal gotowosci." -ForegroundColor Gray
kubectl wait --for=condition=available --timeout=90s deployment/crm-frontend -n crm

# 5. Активація Ingress
kubectl apply -f k8s/crm-ingress.yaml -n crm

Write-Host "`nGotowe! Strona dostepna pod adresem: http://crm.local" -ForegroundColor Green
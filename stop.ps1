Write-Host "Zatrzymywanie serwisow projektu CRM..." -ForegroundColor Red

kubectl delete deployment crm-app crm-frontend mysql -n crm --wait=true
kubectl delete service crm-app crm-frontend mysql -n crm --wait=true
kubectl delete ingress crm-ingress -n crm --wait=true

Write-Host "Wszystkie zasoby zostaly zatrzymane." -ForegroundColor Green

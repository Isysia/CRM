# 🐳 Docker Setup Guide - CRM System

## Architektura
```
┌─────────────────────────────────────────┐
│     Docker Compose Environment          │
│                                         │
│  ┌──────────────┐    ┌──────────────┐  │
│  │  MySQL 8.0   │◄───┤  Spring Boot │  │
│  │  (Port 3306) │    │  (Port 8080) │  │
│  └──────────────┘    └──────────────┘  │
│         │                    │          │
│    [Volume]            [Health Check]   │
└─────────────────────────────────────────┘
```

---

## 📋 Wymagania

- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Maven**: 3.9+ (dla local build)
- **Java**: 17+

Sprawdź wersje:
```bash
docker --version
docker-compose --version
```

---

## 🚀 Szybki Start

### 1. Sklonuj repozytorium
```bash
git clone <your-repo-url>
cd Customer\ Relationship\ Management
```

### 2. Uruchom wszystko jedną komendą
```bash
docker-compose up -d
```

Sprawdź status:
```bash
docker-compose ps
```

### 3. Sprawdź logi
```bash
# Wszystkie serwisy
docker-compose logs -f

# Tylko aplikacja
docker-compose logs -f app

# Tylko MySQL
docker-compose logs -f mysql
```

### 4. Testuj API
```bash
# Health check
curl http://localhost:8080/actuator/health

# Login (Basic Auth)
curl -u admin:admin123 http://localhost:8080/api/customers
```

---

## 🗄️ Dostęp do Bazy Danych

### Przez Docker
```bash
docker exec -it crm-mysql mysql -u crm_user -pcrm_password crm_db
```

### Przez MySQL Client (lokalnie)
```bash
mysql -h 127.0.0.1 -P 3306 -u crm_user -pcrm_password crm_db
```

### Pokaż tabele
```sql
SHOW TABLES;
DESCRIBE customers;
SELECT * FROM customers LIMIT 10;
```

---

## 🛠️ Komendy Docker

### Zarządzanie kontenerami
```bash
# Start
docker-compose up -d

# Stop
docker-compose down

# Restart
docker-compose restart

# Stop + usuń volumes (UWAGA: usunie dane!)
docker-compose down -v

# Rebuild aplikacji
docker-compose up -d --build app
```

### Logi
```bash
# Tail logs
docker-compose logs -f app

# Ostatnie 100 linii
docker-compose logs --tail=100 app

# Od określonego czasu
docker-compose logs --since 30m app
```

### Wejście do kontenera
```bash
# Shell w kontenerze aplikacji
docker exec -it crm-app sh

# Shell w kontenerze MySQL
docker exec -it crm-mysql bash
```

---

## 🔧 Konfiguracja

### Zmiana zmiennych środowiskowych

Edytuj `docker-compose.yml`:
```yaml
environment:
  SPRING_DATASOURCE_USERNAME: nowy_user
  SPRING_DATASOURCE_PASSWORD: nowe_haslo
```

Lub utwórz `.env` file:
```bash
MYSQL_USER=nowy_user
MYSQL_PASSWORD=nowe_haslo
```

### Zmiana portów
```yaml
ports:
  - "8081:8080"  # localhost:8081 → container:8080
```

---

## 🧪 Testowanie

### 1. Sprawdź czy MySQL działa
```bash
docker exec crm-mysql mysqladmin ping -h localhost -u root -prootpassword
```

### 2. Sprawdź czy Liquibase wykonał migracje
```bash
docker exec -it crm-mysql mysql -u crm_user -pcrm_password crm_db -e "SHOW TABLES;"
```

Powinieneś zobaczyć:
```
customers
offers
tasks
users
user_roles
databasechangelog
databasechangeloglock
```

### 3. Testuj API endpoints
```bash
# GET wszystkich klientów (wymaga Basic Auth)
curl -u admin:admin123 http://localhost:8080/api/customers

# POST nowy klient
curl -u manager:manager123 -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jan",
    "lastName": "Kowalski",
    "email": "jan.kowalski@example.pl",
    "phone": "+48501234567",
    "status": "ACTIVE"
  }'
```

---

## 🐛 Troubleshooting

### Problem: Aplikacja nie może połączyć się z MySQL
```bash
# Sprawdź czy MySQL działa
docker-compose ps mysql

# Sprawdź health check
docker inspect crm-mysql | grep -A 5 Health

# Sprawdź logi MySQL
docker-compose logs mysql
```

### Problem: Port 3306 już zajęty
```bash
# Sprawdź co używa portu
netstat -ano | findstr :3306  # Windows
lsof -i :3306                  # Linux/Mac

# Zmień port w docker-compose.yml
ports:
  - "3307:3306"
```

### Problem: Out of memory podczas buildu
```bash
# Zwiększ memory dla Docker Desktop
# Settings → Resources → Memory → 4GB+

# Lub zbuduj lokalnie i użyj JAR
mvn clean package -DskipTests
docker build -t crm-app .
```

### Restart od zera
```bash
# Stop wszystko + usuń volumes
docker-compose down -v

# Usuń obrazy
docker rmi crm-app

# Rebuild i start
docker-compose up -d --build
```

---

## 📊 Monitoring

### Actuator Endpoints
```bash
# Health
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Database health
curl http://localhost:8080/actuator/health/db
```

### Docker Stats
```bash
# CPU/Memory usage
docker stats crm-mysql crm-app
```

---

## 🔒 Security Notes

⚠️ **UWAGA**: Te dane dostępowe są tylko dla developmentu!

Dla **PRODUKCJI** zmień:
- ✅ Hasła do bazy danych
- ✅ Spring Security credentials
- ✅ Użyj HTTPS
- ✅ Włącz SSL dla MySQL
- ✅ Ustaw `SPRING_PROFILES_ACTIVE=prod`

---

## 📦 Build & Deploy

### Local build (bez Docker)
```bash
mvn clean package -DskipTests
java -jar target/*.jar --spring.profiles.active=prod
```

### Docker build (ręcznie)
```bash
# Build image
docker build -t crm-app:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/crm_db \
  --network crm-network \
  crm-app:latest
```

### Push do Registry
```bash
# Tag image
docker tag crm-app:latest <your-registry>/crm-app:1.0.0

# Push
docker push <your-registry>/crm-app:1.0.0
```

---

## 🔗 Links

- Spring Boot App: http://localhost:8080
- Health Check: http://localhost:8080/actuator/health
- MySQL: `localhost:3306`

---

## 📝 Uwagi

- **Liquibase** automatycznie tworzy tabele przy pierwszym uruchomieniu
- **Dane testowe** są wczytywane tylko w profilu `dev` (context=dev)
- **Volumes** przechowują dane MySQL między restartami
- **Health checks** zapewniają że baza jest gotowa przed startem aplikacji

---

## ✅ Checklist przed wdrożeniem

- [ ] Zmień hasła produkcyjne
- [ ] Włącz SSL dla MySQL
- [ ] Skonfiguruj backup bazy danych
- [ ] Ustaw odpowiednie resource limits
- [ ] Włącz monitoring (Prometheus/Grafana)
- [ ] Skonfiguruj log aggregation
- [ ] Przejrzyj Spring Security config
- [ ] Test recovery scenarios

---

**Powodzenia! 🚀**
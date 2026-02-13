# System CRM oparty na architekturze Microservices i Kubernetes

W pełni funkcjonalny system do zarządzania relacjami z klientami (CRM), zaprojektowany w architekturze Domain-Driven Design (DDD) i wdrożony na platformie Kubernetes. Projekt został zrealizowany jako praca inżynierska.

## Stos technologiczny
* **Backend:** Java 17, Spring Boot 3.4.4, Spring Security, Hibernate
* **Frontend:** React 18, Vite, TailwindCSS, Axios
* **Baza danych:** MariaDB 11.2, Liquibase (migracje)
* **Pamięć podręczna (Cache):** Redis 7.2
* **DevOps & Infrastruktura:** Docker, Kubernetes (K8s), Nginx
* **Monitoring:** Prometheus, Grafana, Spring Boot Actuator

## Główne funkcjonalności
* **Klienci (Customers):** Zarządzanie bazą klientów z przypisywaniem statusów (LEAD, ACTIVE, INACTIVE).
* **Oferty (Offers):** Obsługa lejka sprzedażowego (pipeline) z 5 statusami (m.in. DRAFT, SENT, ACCEPTED).
* **Zadania (Tasks):** Zarządzanie zadaniami z priorytetami i terminami wykonania, automatyczne wykrywanie opóźnień.
* **Kontrola dostępu (RBAC):** Trzy poziomy uprawnień (USER, MANAGER, ADMIN) z zabezpieczeniem przed eskalacją roli.

## Wymagania wstępne
Aby uruchomić projekt lokalnie, musisz mieć zainstalowane:
* **Docker Desktop** (musi być stale uruchomiony w tle)
* Aktywowany lokalny klaster **Kubernetes** (np. wbudowany w Docker Desktop)
* Narzędzie wiersza poleceń `kubectl`
* PowerShell (do uruchamiania skryptów wdrożeniowych)

## Uruchomienie lokalne

1. **Sklonuj repozytorium:**
   git clone [https://github.com/lsysia/CRM.git](https://github.com/lsysia/CRM.git)

3. **Uruchom aplikację:**
   Użyj dedykowanego skryptu, który wdroży sekrety, bazę danych, backend oraz frontend z oczekiwaniem na gotowość poszczególnych komponentów:
   `./start.ps1`

4. **Krok 3: Zarządzanie aplikacją**
   Aby zatrzymać i usunąć zasoby z klastra K8s, użyj polecenia:
   `./stop.ps1`
   
   Aby przebudować i zrestartować projekt, użyj polecenia:
   `./rebuild.ps1`

**Dostęp do systemu:**
   Po pomyślnym wdrożeniu, system będzie dostępny pod adresem: http://crm.local (wymaga odpowiedniej konfiguracji Ingress Controller lub modyfikacji pliku hosts).

**Ważne: Konfiguracja pliku hosts**
Aby adres `http://crm.local` działał poprawnie, system operacyjny musi wiedzieć, że ta domena wskazuje na Twój lokalny klaster Kubernetes. Należy zmapować ten adres na `localhost` (127.0.0.1).

**Dla systemu Windows:**
1. Uruchom program **Notatnik (Notepad) jako Administrator** (kliknij prawym przyciskiem myszy na ikonę Notatnika i wybierz "Uruchom jako administrator").
2. W Notatniku wybierz *Plik -> Otwórz* i przejdź do ścieżki:
   `C:\Windows\System32\drivers\etc\hosts`
   *(Upewnij się, że w oknie wyboru plików w prawym dolnym rogu masz wybrane "Wszystkie pliki (*.*)", a nie tylko dokumenty tekstowe).*
3. Na samym końcu tego pliku dopisz nową linijkę:
   `127.0.0.1 crm.local`
4. Zapisz plik (Ctrl+S) i zamknij Notatnik.

**Dla systemu macOS / Linux:**
1. Otwórz terminal.
2. Wpisz polecenie: `sudo nano /etc/hosts` i podaj swoje hasło.
3. Za pomocą strzałek zjedź na sam dół i dopisz:
   `127.0.0.1 crm.local`
4. Zapisz zmiany naciskając `Ctrl+O`, następnie `Enter`, a na koniec wyjdź wciskając `Ctrl+X`.

Po tej operacji aplikacja powinna być od razu dostępna w przeglądarce pod adresem http://crm.local.

## Domyślne konta testowe
Baza danych jest automatycznie inicjalizowana przy pierwszym uruchomieniu za pomocą DataInitializer. Dostępne są następujące konta:
* **Admin:** admin / password (Rola: ADMIN)
* **Manager:** manager / password (Rola: MANAGER)
* **User:** user / password (Rola: USER)
* **Nowi użytkownicy:** Każda osoba, która zarejestruje się samodzielnie poprzez formularz, automatycznie otrzymuje rolę USER.

## Monitoring
Aplikacja posiada zintegrowany stos monitoringu zapewniający pełną obserwowalność:
* **Grafana:** Dostępna przez NodePort 30300 (zawiera gotowy dashboard Spring Boot 2.1 System Monitor).
* **Prometheus:** Dostępny przez NodePort 30090.

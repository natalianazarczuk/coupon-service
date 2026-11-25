# Coupon Service

Serwis REST umożliwiający tworzenie i realizację kuponów rabatowych, przygotowany w ramach zadania rekrutacyjnego.

## Technologie

- Java, Spring Boot
- Spring Data JPA, PostgreSQL
- Flyway
- Gradle
- JUnit 5, Mockito
- FreeIPAPI

## Funkcjonalności

- Tworzenie kuponów z unikalnym kodem, niezależnie od wielkości liter.
- Realizacja kuponów z ograniczoną liczbą użyć.
- Weryfikacja kraju użytkownika na podstawie adresu IP.
- Jednokrotne wykorzystanie danego kuponu przez użytkownika.
- Obsługa współbieżnych prób realizacji kuponów.

## Uruchomienie aplikacji

Wymagana jest działająca instancja PostgreSQL oraz skonfigurowane zmienne środowiskowe:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/coupons
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

Uruchomienie aplikacji:

```bash
./gradlew bootRun
```

Migracje bazy danych wykonywane są automatycznie przez Flyway.

## REST API

### Tworzenie kuponu

`POST /coupons`

```json
{
  "code": "Kupon2026",
  "maxUsages": 100,
  "country": "PL"
}
```

Odpowiedź: `201 Created`

### Realizacja kuponu

`POST /coupons/redeem`

```json
{
  "code": "Kupon2026",
  "userUuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

Odpowiedź: `200 OK`

### Obsługa błędów

| HTTP Status | Przyczyna |
|---|---|
| 400 Bad Request | Nieprawidłowe dane wejściowe |
| 403 Forbidden | Niedozwolony kraj |
| 404 Not Found | Kupon nie istnieje |
| 409 Conflict | Duplikat kodu, wyczerpany limit lub ponowna realizacja |
| 503 Service Unavailable | Nie udało się ustalić kraju użytkownika |

## Decyzje techniczne

- Unikalność kodów kuponów zapewnia indeks bazodanowy wykorzystujący `LOWER(code)`.
- Pessimistic locking zabezpiecza przed przekroczeniem limitu użyć podczas współbieżnych realizacji.
- Aktualizacja licznika użyć i zapis realizacji odbywają się w ramach jednej transakcji.
- Ograniczenie `UNIQUE (coupon_id, user_uuid)` zabezpiecza przed wielokrotnym wykorzystaniem kuponu przez tego samego użytkownika.
- Wywołanie zewnętrznego API geolokalizacji odbywa się poza transakcją bazodanową.

## Testy

Testy jednostkowe:

```bash
./gradlew test
```

Testy integracyjne:

```bash
./gradlew integrationTest
```

Testy obejmują logikę biznesową, operacje bazodanowe, komunikację HTTP oraz współbieżną realizację kuponów. Odpowiedzi zewnętrznego API są symulowane w testach.

## Ograniczenia

- FreeIPAPI posiada ograniczenia liczby zapytań.
- Geolokalizacja wymaga publicznego adresu IP. Realizacja kuponu z localhost może zakończyć się błędem.

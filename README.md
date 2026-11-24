# Coupon Service

REST-owy serwis odpowiedzialny za zarządzanie kuponami rabatowymi

## Tech Stack

- Java 21
- Spring Boot
- Gradle
- PostgreSQL
- Flyway
- Docker Compose
- Testcontainers

## Funkcjonalności

- Rejestracja użycia kuponu przez użytkownika
- Tworzenie nowego kuponu

## Kupon:

- kod kuponu (unikalny, case-insensitive)
- data utworzenia,
- maksymalna liczba możliwych użyć
- bieżąca liczba użyć, 
- kraj, dla którego kupon jest przeznaczony

## Wymagania biznesowe

- kod kuponu unikalny, wielkość znaków nie ma znaczenia
- wykorzystanie kuponu ma max użyć, kto pierwszy ten lepszy
- kupon może być zrealizowany jedynie w kraju, który ma przypisany (usługa IP) 
- kupon jest walidowany przed realizacją: zwraca odpowiednie informacje, gdy kupon nie istnieje, jest użyty z niedozwolonoego kraju, lub był wcześniej zużyty
- jeden użytkownik może wykorzystać kupon tylko raz (request powinien zawierać id użytkonika i kod kuponu)
# Diridari

Android-App mit einem Homescreen-Widget, das die Tage bis zum nächsten Stichtag herunterzählt.

## Stichtag

Der Stichtag ist der Bankarbeitstag, nach dem bis zum 1. des Folgemonats noch genau
**3 Bankarbeitstage** liegen.

Beispiel November 2026: Der 1.12. ist ein Dienstag. Dazwischen liegen der 26.11., 27.11. und 30.11.,
also ist der Stichtag Mittwoch, der **25.11.**

Bankarbeitstage sind Montag bis Freitag, ohne:

- die gesetzlichen Feiertage in Bayern: Neujahr, Heilige Drei Könige, Karfreitag, Ostermontag,
  1. Mai, Christi Himmelfahrt, Pfingstmontag, Fronleichnam, Mariä Himmelfahrt, Tag der Deutschen
  Einheit, Allerheiligen, 1. und 2. Weihnachtstag
- 24.12. und 31.12. (Banken geschlossen)

Ist der Stichtag des laufenden Monats vorbei, zählt das Widget auf den Stichtag des nächsten Monats.
Am Stichtag selbst zeigt es „Heute!“.

Die Logik steckt in [`BankCalendar.kt`](app/src/main/java/de/diridari/countdown/BankCalendar.kt) und
[`Deadline.kt`](app/src/main/java/de/diridari/countdown/Deadline.kt); die Anzahl der Tage ist
`Deadline.BANK_DAYS_BEFORE`.

## Bauen

Projekt in Android Studio öffnen (lädt Gradle 8.11.1 und das Android SDK selbst), dann `app` starten.
Das Widget über den Homescreen hinzufügen. Die App selbst zeigt die Stichtage der nächsten zwölf Monate.

Unit-Tests: `./gradlew test`

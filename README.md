# Diridari

Android-App mit Countdown-Widgets für den Homescreen.

- Mehrere Countdowns, jeder mit eigenem Namen: Regel **Gehalt** (siehe unten) oder ein frei wählbares Datum
- Form wählbar: Kreis, Dreieck, Viereck oder **Paul** (Herz)
- Farbe (8 zur Auswahl, Standard je Form) und Textgröße (Klein, Mittel, Groß) pro Countdown
- Um die Form läuft ein Ladering, der den Anteil der verbleibenden Tage zeigt
  (Gehalt: vom letzten bis zum nächsten Stichtag, Datum: vom Anlegen bis zum Datum)
- Größe frei veränderbar, ab 1×1
- Anlegen über „Neuer Countdown“ in der App oder durch Ziehen des Widgets auf den Homescreen;
  Tipp aufs Widget öffnet die Einstellungen

`./gradlew test` rendert alle Formen zusätzlich als PNG nach `app/build/previews`.

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

Ohne Android Studio, über die Kommandozeile:

- JDK 17 oder neuer (z.B. das von Rider: `%LOCALAPPDATA%\Programs\Rider\jbr`) als `JAVA_HOME`
- Android SDK mit `platforms;android-35` und `build-tools;35.0.0`, Pfad in `local.properties`:
  `sdk.dir=C\:\\Users\\<user>\\Android\\Sdk`

```
./gradlew test           # Unit-Tests
./gradlew assembleDebug  # APK: app/build/outputs/apk/debug/app-debug.apk
```

Die APK aufs Handy kopieren und installieren, dann das Widget über den Homescreen hinzufügen.
Die App selbst listet alle Countdowns; per Tipp lassen sie sich bearbeiten.

# Penguin Visuals (Fabric 1.21.4)

Klientowy mod z pingwinim motywem. Menu otwierasz klawiszem **9** (zmienisz w Sterowaniu, kategoria "Penguin Visuals").

## Funkcje
- **Licencja** - bez klucza mod nic nie robi, po wciśnięciu 9 pojawia się ekran aktywacji
- **Niebo** - presety (niebieskie, czerwone, fioletowe, zielone, różowe, pomarańczowe, lodowe, tęcza) i własny kolor RGB; cykl dnia i nocy zostaje
- **Walka** - dźwięk po uderzeniu: 12 własnych dźwięków (hitmarker, moneta, laser, katana, bonk, szkło, zap, headshot, retro 8-bit, bas 808, bąbel, magia) i 4 z gry; głośność, ton, próbka; cząsteczki
- **Cel** - po najechaniu na gracza: serduszka z jego HP nad głową oraz panel z nickiem i HP (domyślnie lewy górny róg; miejsce i przesunięcie X/Y do zmiany w menu), zasięg do 64 bloków
- **Obrys** - animowane obramowanie wokół gracza: 5 stylów (kometa, płynąca, przerywana, kropki, narożniki), kolory z tęczą, szybkość i grubość, z podglądem w menu
- **HUD** - znak wodny, FPS, współrzędne
- **Motyw** - 11 kolorów interfejsu i efekt tła menu: śnieg, deszcz, gwiazdy, iskry, bąbelki, matrix lub brak (opcjonalnie też w grze)
- **GUI** - animowane otwieranie, zaokrąglone rogi i cienie, śnieg w tle, ikony zakładek z płynnym wskaźnikiem, animowane przełączniki i suwaki, kafelki kolorów motywu, przewijanie kółkiem myszy
- Ustawienia zapisują się w `config/penguinvisuals.json`

## Budowanie
Wymagany JDK 21.

Najprościej: otwórz folder w IntelliJ IDEA (Open -> build.gradle), poczekaj aż się zsynchronizuje i uruchom zadanie `build`.

Z terminala (wymaga zainstalowanego Gradle 8.12+):

    gradle wrapper --gradle-version 8.12
    ./gradlew build        (Windows: gradlew.bat build)

Gotowy plik: `build/libs/penguin-visuals-1.0.0.jar` (wrzuć do `mods/` razem z Fabric API).

## Licencje
Do moda trafiają tylko hashe SHA-256 kluczy (`assets/penguinvisuals/licenses.hashes`), same klucze są w osobnym pliku `licencje.txt`.
Format klucza: `PENG-XXXX-XXXX-XXXX-XXXX`.

Nowe klucze: policz `sha256("penguin-v1:" + KLUCZ)` (małe litery hex) i dopisz linijkę do `licenses.hashes`.

# Лабораторна №8. GRASP, готельне бронювання (Java)

Команда з двох студентів. Студент А: домен і поліморфізм (завдання 2, 4). Студент Б: CLI, контролер, інфраструктура (завдання 3). Завдання 1, 5, 6 робили разом.

## Структура

    legacy/LegacyHotelManager.java   вихідний поганий код (один файл)
    src/hotel/
        Main.java                    composition root
        SelfCheck.java               самоперевірки (18 штук)
        domain/                      Student A: сутності, політики, інтерфейси репозиторіїв
        application/                 Student B: контролери, фабрика, DTO
        cli/                         Student B: консоль
        infrastructure/              Student B: файл, SMS, список номерів
    docs/adr/                        ADR 0001-0004
    docs/METRICS.md, metrics.py      CBO, LCOM4, Instability
    docs/PEER_REVIEW.md, DEFENSE.md  крос-рев'ю і підготовка до захисту

## Збірка і запуск (JDK 17+), з кореневої теки

Linux / macOS / Git Bash:

    javac -encoding UTF-8 -d bin $(find src legacy -name '*.java')
    java -cp bin hotel.Main
    java -cp bin hotel.SelfCheck

PowerShell:

    javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse src,legacy -Filter *.java).FullName
    java -cp bin hotel.Main
    java -cp bin hotel.SelfCheck

Метрики:

    python3 docs/metrics.py src
    python3 docs/metrics.py legacy

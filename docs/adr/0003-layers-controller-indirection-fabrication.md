# ADR-0003: Тонкий CLI, Use-Case Controller, інтерфейси, штучні класи і розкладка по файлах

- Статус: прийнято
- Дата: 2026-10-24
- Автор: Студент Б (завдання 3)

## Контекст

`createBookingFromConsole()` змішує UI, бізнес-логіку і збереження (ADR-0001, пункти 4, 7, 10, 12). У першій версії рефакторингу було 41 файл по одному типу, і для проєкту з трьома use-case'ами це було забагато.

## Варіанти (QOC)

**Q1. Хто перший приймає команду після консолі?**

- A: сам консольний клас
- B: один `BookingService` на все
- C: тонкий `BookingController`, який делегує `BookingFactory` і `BookingRegistrar`

| | без I/O | CBO | Cohesion |
|---|---|---|---|
| A | ні | великий | погано |
| B | так | 6-7 | знову god service |
| C | так | 4 | добра |

Обрано C.

**Q2. Як контролер збереже бронювання і надішле SMS, не знаючи про файли?**

- A: `new FileWriter` напряму
- B: інтерфейси `BookingRepository`, `NotificationSender` (Indirection)
- C: події і шина

Обрано B, C для лабораторної завелике.

**Q3. Як розкласти типи по файлах?**

- A: один тип на файл (як у першій версії)
- B: інтерфейс і його реалізації в одному файлі, реалізації package-private, дрібні допоміжні класи поруч з тим, кому вони потрібні
- C: усе в одному файлі на шар

| | файлів | реалізації сховані | зручно читати |
|---|---|---|---|
| A | 41 | ні, усі public | багато стрибків між файлами |
| B | 22 | так | добре |
| C | 4-5 | так | файли по 300+ рядків |

Обрано B. Це відходить від "один тип на файл", зате Main і CLI не бачать конкретних класів (Protected Variations), а повʼязані речі лежать поруч.

## Рішення

Шари і залежності: `cli -> application -> domain <- infrastructure`.

- **CLI** (`hotel.cli`):
  - `ConsoleIO.java`: `ConsoleIO` (Scanner і println), `ConsoleCommandParser` (текст у `BookingRequest`), `ReceiptPresenter` (друк результату). Останні два package-private.
  - `ConsoleCommand.java`: інтерфейс пункту меню і три команди (`BookRoomCommand`, `ListRoomsCommand`, `ExitCommand`), створюються через `ConsoleCommand.bookRoom(...)`, `.listRooms(...)`, `.exit(...)`.
  - `ConsoleUi.java`: меню у вигляді `Map<String, ConsoleCommand>`. У CLI немає жодної формули.
- **Controller**: `BookingController` і `RoomQueryController` приймають DTO і нічого не друкують. `BookingController.book(request)` це `registrar.register(factory.create(request))`.
- **Creator**: `BookingFactory` (разом із package-private `StayPricingAssembler`, який перетворює прапорці запиту на доменні об'єкти).
- **Indirection**: `BookingRepository`, `RoomRepository` (domain) і `NotificationSender` (application). Реалізації в `Infrastructure.java` package-private, зовні доступні тільки через `Infrastructure.bookingsFile(...)`, `.rooms()`, `.smsNotifier(...)`.
- **Pure Fabrication**: `ConsoleIO`, `ConsoleCommandParser`, `ReceiptPresenter`, `BookingRegistrar`, `StayPricingAssembler`, `FileBookingRepository`.
- `Main` це composition root, єдине місце, де все зв'язується.
- `BookingResult` і `RoomQueryController.RoomInfo` це DTO, тому CLI не залежить від доменних класів.

## Наслідки

- Плюс: контролер тестується з підробними `FakeRepo` і `FakeSender` (див. `SelfCheck`).
- Плюс: файл і SMS тепер реальні (файл пишеться в `bookings.dat`), а не `println`.
- Плюс: `Main` залежить від 9 типів замість 18.
- Мінус: `ConsoleCommand` (CBO 8) і `Infrastructure` (CBO 6) це фабричні точки, які знають свої реалізації. Це не сутності і не контролери, тому норматив ≤ 4 до них не застосовуємо, але вони є.
- Мінус: `BookingController` тримає ланцюжок `registrar.register(factory.create(request))`. Це не порушення Деметри (виклики методів власних полів), але читається щільно.

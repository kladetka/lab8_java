# Метрики до і після рефакторингу

Скрипт: `python3 docs/metrics.py src` (після) і `python3 docs/metrics.py legacy` (до).
CBO рахується за правилом з методички: типи `java.*` не враховуються (Scanner, BigDecimal, List, Map теж).

## 1. CBO

| Клас | До | Після | Норматив ≤ 4 |
|------|----|-------|--------------|
| LegacyHotelManager (формально) | 3 (Booking, Guest, Room) | не існує | |
| LegacyHotelManager (ефективно, з прихованими Scanner, консоль-вивід, файл bookings.dat, SMS) | 7 | | |
| legacy Booking | 2 | | |
| BookingController | | 4 | так |
| BookingRegistrar | | 4 | так |
| StayPricingAssembler | | 4 | так |
| Booking (domain) | | 3 | так |
| StayPricing (domain) | | 4 | так |
| Room, Guest, Room.Category, Booking.Status | 0 | 0 | так |
| RoomQueryController | | 1 | так |
| BookRoomCommand (CLI) | | 4 | так |
| ConsoleUi (CLI) | | 2 | так |
| BookingFactory (Creator) | | 6 | ні, пояснення в ADR-0003 |
| ConsoleCommand (фабрика команд) | | 8 | не сутність і не контролер, ADR-0003 |
| Infrastructure (фабрика реалізацій) | | 6 | не сутність і не контролер, ADR-0003 |
| Main (composition root) | | 9 (було 18) | не застосовується |

Максимум серед сутностей і контролерів = 4.

## 2. LCOM4 (нетривіальні методи, getter-и, конструктори і статичні фабрики виключені)

| Клас | Методи в графі | Компоненти | LCOM4 |
|------|---------------|------------|-------|
| LegacyHotelManager (до) | createBookingFromConsole | 1 метод, але 8 обов'язків | 1 формально |
| legacy Guest, Room, Booking (до) | немає методів | | не визначено |
| Booking | roomTotal, servicesTotal, touristTax, grandTotal, confirm (спільне поле pricing, confirm викликає grandTotal) | 1 | 1 |
| StayPricing | roomTotal, servicesTotal, touristTax, grandTotal (спільні nights, виклики) | 1 | 1 |
| Room | costFor | 1 | 1 |
| Guest | немає логіки (валідація в `register`) | 1 | 1 |
| ConsoleCommandParser | readBookingRequest викликає readInt і readYesNo | 1 | 1 |
| ReceiptPresenter | show, showError (поле io), money (виклик з show) | 1 | 1 |
| ConsoleIO | ask, println (поле out) | 1 | 1 |
| InMemoryRoomRepository | getByNumber, findAll (поле rooms) | 1 | 1 |
| Реалізації SeasonPolicy, AdditionalService | `name()` тривіальний і виключений, лишається apply / cost | 1 | 1 |
| BookingController, BookingRegistrar, BookingFactory, StayPricingAssembler, FileBookingRepository, всі команди | по одному методу | 1 | 1 |
| Infrastructure, ConsoleCommand (тільки статичні фабрики) | статичні фабрики виключені | | 1 за домовленістю, формально 3 |

Примітка 1: якщо в граф `Booking` включити прості getter-и, то `id()`, `guestName()` і `guestPhone()` не пов'язані з рештою через спільні поля, і LCOM4 стане 3. Методичка дозволяє такі методи виключати.

Примітка 2: `Infrastructure` не має полів, тому якби ми рахували її три статичні методи, вийшло б 3. Це свідомий компроміс ради меншої кількості файлів (ADR-0003), а не доведена згуртованість.

Важливо: LCOM4 для legacy дає 1 тільки тому, що там один метод. Реальна проблема видна з кількості обов'язків: 8 у `createBookingFromConsole` (консоль, створення об'єктів, ціна за сезоном, послуги, збір, форматування, файл, SMS) проти 1 в кожному новому класі.

## 3. Instability по пакетах (Stable Dependencies)

| Пакет | Ca | Ce | I |
|-------|----|----|---|
| hotel.domain | 10 | 0 | 0.00 |
| hotel.application | 8 | 9 | 0.53 |
| hotel.cli | 1 | 4 | 0.80 |
| hotel.infrastructure | 1 | 5 | 0.83 |
| hotel (Main) | 0 | 9 | 1.00 |
| legacy (до) | один пакет без шарів | | не визначено |

Залежності йдуть від менш стабільного до більш стабільного: cli (0.80) і infrastructure (0.83) залежать від application (0.53) і domain (0.00), application залежить тільки від domain, а domain ні від кого не залежить.

## 4. Підсумкова таблиця

| Показник | До | Після |
|----------|----|-------|
| Кількість обов'язків у ключовому класі | 8 | 1 |
| Макс. CBO сутностей і контролерів | 3 (формально), 7 (ефективно) | 4 |
| LCOM4 ключових класів | 1 (один метод, але "god method") | 1 для всіх |
| if-else по типу чи сезону в домені | 5 (3 за сезоном і 2 за послугами) | 0 |
| Публічні поля | 19 | 0 |
| Файлів з кодом (без тестів і legacy) | 4 | 21 (у першій версії рефакторингу було 40) |
| Тестованість без консолі | немає | є (18 перевірок) |
| I для domain | не визначено | 0.00 |

Два `if` по прапорцях сніданок/паркування залишились у `StayPricingAssembler` (application), бо це переклад галочок із запиту на об'єкти послуг, а не бізнес-правило.

# Підготовка до захисту: усі 9 патернів GRASP

Кожен з нас має вміти пояснити будь-який пункт, незалежно від того, хто його писав.

| Патерн | Де в коді | Що було в legacy | Аргумент |
|--------|-----------|------------------|----------|
| Information Expert | `StayPricing`, `Room.costFor`, `Booking` | формули в менеджері | дані про ночі, сезон, послуги лежать у `StayPricing`, тому формули там |
| Creator | `Guest.register`, `Booking.create`, `StayPricing.of`, `BookingFactory` | `new` і заповнення полів ззовні | об'єкт одразу валідний, створює той, хто має дані для ініціалізації |
| Controller | `BookingController`, `RoomQueryController` | метод менеджера читав консоль | перший об'єкт після UI, без I/O, приймає DTO |
| Low Coupling | CBO ≤ 4, залежність через інтерфейси | все в одному місці | зміна файлу чи SMS не чіпає домен |
| High Cohesion | LCOM4 = 1, один обов'язок на клас | 8 обов'язків в одному методі | кожен клас міняється з однієї причини |
| Pure Fabrication | `ConsoleIO`, `ConsoleCommandParser`, `ReceiptPresenter`, `BookingRegistrar`, `StayPricingAssembler`, `FileBookingRepository` | відсутні | технічні класи, яких нема в домені, щоб не навантажувати сутності |
| Indirection | `BookingRepository`, `RoomRepository`, `NotificationSender`, `ConsoleCommand` | прямі виклики | проміжний інтерфейс між шарами |
| Polymorphism | `SeasonPolicy`, `AdditionalService`, `ConsoleCommand` | каскади if-else | поведінка залежить від типу об'єкта, а не від `if` |
| Protected Variations | `SeasonPolicy`, `TouristTaxPolicy`, `AdditionalService`, репозиторії, `NotificationSender`, `Infrastructure` | числа і SMS прямо в коді | нестабільні місця сховані за стабільним інтерфейсом, реалізації package-private |

## Питання, які можуть задати

1. Чому `BookingController` не друкує? Бо Controller це Application Service, I/O робить CLI.
2. Чому `Booking` не рахує сам, а делегує `StayPricing`? Щоб не збільшувати CBO і не роздувати клас, `StayPricing` знає сезон, послуги і податок.
3. Чому ви виключили getter-и з LCOM4? Так дозволяє методичка, але ми показали, що буде якщо включити (LCOM4=3).
4. У legacy CBO вже ≤ 4, то де покращення? Приховані залежності (консоль, файл, SMS) стали явними інтерфейсами, обов'язків 8 до 1, формули в сутностях, Instability показує правильний напрямок залежностей.
5. Чому у `BookingFactory` CBO=6? Creator знає те, що створює (Booking, Guest), звідки бере дані (RoomRepository, BookingRequest) і через що збирає ціну (StayPricingAssembler, TouristTaxPolicy). Це компроміс.
6. Це порушення Деметри `registrar.register(factory.create(request))`? Ні, обидва об'єкти це поля контролера, ми не ходимо в чужі поля.
7. Що зміниться, якщо додати новий сезон? Новий клас у `SeasonPolicy.java` і один рядок у `SeasonPolicies`.
8. Що зміниться, якщо SMS замінити на email? Новий клас, що реалізує `NotificationSender`, метод у `Infrastructure` і одна зміна в `Main`.
9. Чому гроші `BigDecimal`? Щоб не було похибок `double`.
10. Як довели, що ціни не змінились? Тест `legacy == new` для Low, High і Peak.
11. Чому в одному файлі кілька класів? Реалізації інтерфейсів package-private, лежать поруч з інтерфейсом і зовні недоступні, ззовні видно тільки контракт (ADR-0003, Q3).
12. Чому `SeasonPolicy` знає про свої реалізації? Це ціна за приховані реалізації. Вони в одному пакеті, а зовнішній код бачить тільки `SeasonPolicy.byName(...)`.

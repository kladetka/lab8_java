package hotel.application;

// Use-case Controller: перший об'єкт після CLI. Приймає DTO, створення віддає фабриці,
// підтвердження і збереження реєстратору. Ні консолі, ні формул тут нема.
public class BookingController {
    private final BookingFactory factory;
    private final BookingRegistrar registrar;

    public BookingController(BookingFactory factory, BookingRegistrar registrar) {
        this.factory = factory;
        this.registrar = registrar;
    }

    public BookingResult book(BookingRequest request) {
        return registrar.register(factory.create(request));
    }
}

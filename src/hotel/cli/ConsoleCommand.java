package hotel.cli;

import hotel.application.BookingController;
import hotel.application.RoomQueryController;

// Пункт меню. Замість switch у головному циклі (Polymorphism).
// Самі команди сховані в цьому файлі, ззовні їх створюють через статичні методи.
public interface ConsoleCommand {
    String key();

    String title();

    // false = програму треба завершити
    boolean execute();

    static ConsoleCommand bookRoom(ConsoleIO io, BookingController controller) {
        return new BookRoomCommand(new ConsoleCommandParser(io), controller, new ReceiptPresenter(io));
    }

    static ConsoleCommand listRooms(ConsoleIO io, RoomQueryController controller) {
        return new ListRoomsCommand(io, controller);
    }

    static ConsoleCommand exit(ConsoleIO io) {
        return new ExitCommand(io);
    }
}

class BookRoomCommand implements ConsoleCommand {
    private final ConsoleCommandParser parser;
    private final BookingController controller;
    private final ReceiptPresenter presenter;

    BookRoomCommand(ConsoleCommandParser parser, BookingController controller, ReceiptPresenter presenter) {
        this.parser = parser;
        this.controller = controller;
        this.presenter = presenter;
    }

    public String key() {
        return "1";
    }

    public String title() {
        return "Забронювати номер";
    }

    public boolean execute() {
        try {
            presenter.show(controller.book(parser.readBookingRequest()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            presenter.showError(e.getMessage());
        }
        return true;
    }
}

class ListRoomsCommand implements ConsoleCommand {
    private final ConsoleIO io;
    private final RoomQueryController controller;

    ListRoomsCommand(ConsoleIO io, RoomQueryController controller) {
        this.io = io;
        this.controller = controller;
    }

    public String key() {
        return "2";
    }

    public String title() {
        return "Показати номери";
    }

    public boolean execute() {
        for (var room : controller.availableRooms()) {
            io.println(room.number() + "  " + room.category() + "  " + room.pricePerNight().toPlainString()
                    + " грн/доба");
        }
        return true;
    }
}

class ExitCommand implements ConsoleCommand {
    private final ConsoleIO io;

    ExitCommand(ConsoleIO io) {
        this.io = io;
    }

    public String key() {
        return "0";
    }

    public String title() {
        return "Вихід";
    }

    public boolean execute() {
        io.println("До побачення!");
        return false;
    }
}

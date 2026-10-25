package hotel.cli;

import hotel.application.BookingRequest;
import hotel.application.BookingResult;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Set;

// Pure Fabrication: Scanner і System.out сидять тільки тут.
public class ConsoleIO {
    private final java.util.Scanner scanner;
    private final PrintStream out;

    public ConsoleIO(InputStream in, PrintStream out) {
        scanner = new java.util.Scanner(in, StandardCharsets.UTF_8);
        this.out = out;
    }

    public String ask(String prompt) {
        out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    public void println(String text) {
        out.println(text);
    }
}

// Текст з консолі -> BookingRequest. Тільки розбір рядків, ніякої бізнес-логіки.
class ConsoleCommandParser {
    private static final Set<String> YES = Set.of("y", "yes", "так", "т", "+");

    private final ConsoleIO io;

    ConsoleCommandParser(ConsoleIO io) {
        this.io = io;
    }

    BookingRequest readBookingRequest() {
        String name = io.ask("Введіть ПІБ гостя: ");
        String phone = io.ask("Введіть телефон (+380...): ");
        String passport = io.ask("Введіть номер паспорта: ");
        int room = readInt("Введіть номер кімнати: ");
        int nights = readInt("Кількість ночей: ");
        String season = io.ask("Введіть сезон (Low/High/Peak): ");
        boolean breakfast = readYesNo("Додати сніданок? (y/n): ");
        boolean parking = readYesNo("Додати паркування? (y/n): ");
        return new BookingRequest(name, phone, passport, room, nights, season, breakfast, parking);
    }

    private int readInt(String prompt) {
        String text = io.ask(prompt);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Очікувалось число, а отримано: " + text);
        }
    }

    private boolean readYesNo(String prompt) {
        return YES.contains(io.ask(prompt).toLowerCase());
    }
}

// Друкує квитанцію і помилки. Формат такий самий, як був у legacy.
class ReceiptPresenter {
    private final ConsoleIO io;

    ReceiptPresenter(ConsoleIO io) {
        this.io = io;
    }

    void show(BookingResult r) {
        io.println("Бронювання #" + r.bookingId() + " для " + r.guestName()
                + "\nКімната: " + r.roomNumber() + " (" + r.roomCategory() + ")"
                + "\nПроживання: " + money(r.roomTotal()) + " грн"
                + "\nПослуги: " + money(r.servicesTotal()) + " грн"
                + "\nТуристичний збір: " + money(r.touristTax()) + " грн"
                + "\nВсього: " + money(r.grandTotal()) + " грн"
                + "\nСтатус: " + r.status());
    }

    void showError(String message) {
        io.println("Помилка: " + message);
    }

    private String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

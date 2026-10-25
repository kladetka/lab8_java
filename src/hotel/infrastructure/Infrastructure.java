package hotel.infrastructure;

import hotel.application.NotificationSender;
import hotel.domain.BookingRepository;
import hotel.domain.Booking;
import hotel.domain.Room;
import hotel.domain.RoomRepository;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

// Усі деталі "зовнішнього світу" (файл, SMS, список номерів) в одному місці.
// Main отримує тільки інтерфейси і не знає, які класи за ними стоять.
public final class Infrastructure {
    private Infrastructure() {
    }

    public static BookingRepository bookingsFile(Path file) {
        return new FileBookingRepository(file);
    }

    public static RoomRepository rooms() {
        return new InMemoryRoomRepository();
    }

    public static NotificationSender smsNotifier(PrintStream out) {
        return new SmsGatewayNotifier(out);
    }
}

// Файлова "БД": по рядку на бронювання (у legacy це був просто println)
class FileBookingRepository implements BookingRepository {
    private final Path file;

    FileBookingRepository(Path file) {
        this.file = file;
    }

    @Override
    public synchronized void save(Booking booking) {
        String line = String.join(";",
                String.valueOf(booking.id()),
                booking.guestName().replace(';', ','),
                booking.guestPhone(),
                String.valueOf(booking.roomNumber()),
                String.valueOf(booking.nights()),
                booking.grandTotal().toPlainString(),
                booking.statusLabel()) + System.lineSeparator();
        try {
            Files.writeString(file, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Не вдалося записати бронювання у " + file, e);
        }
    }
}

class InMemoryRoomRepository implements RoomRepository {
    private final List<Room> rooms = List.of(
            new Room(101, Room.Category.STANDARD, new BigDecimal("1000")),
            new Room(302, Room.Category.DELUXE, new BigDecimal("2000")),
            new Room(501, Room.Category.SUITE, new BigDecimal("4000")));

    @Override
    public Room getByNumber(int roomNumber) {
        return rooms.stream()
                .filter(r -> r.number() == roomNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Номера " + roomNumber + " не існує"));
    }

    @Override
    public List<Room> findAll() {
        return rooms;
    }
}

// Імітація SMS-шлюзу: замість справжнього API друкуємо у потік
class SmsGatewayNotifier implements NotificationSender {
    private final PrintStream out;

    SmsGatewayNotifier(PrintStream out) {
        this.out = out;
    }

    @Override
    public void sendBookingConfirmation(String phone, long bookingId, BigDecimal total) {
        out.println("[SMS] Бронювання #" + bookingId + " підтверджено, до сплати " + total.toPlainString()
                + " грн. Надіслано на номер " + phone);
    }
}

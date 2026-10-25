package hotel;

import hotel.application.BookingController;
import hotel.application.BookingFactory;
import hotel.application.BookingRegistrar;
import hotel.application.RoomQueryController;
import hotel.cli.ConsoleCommand;
import hotel.cli.ConsoleIO;
import hotel.cli.ConsoleUi;
import hotel.domain.TouristTaxPolicy;
import hotel.infrastructure.Infrastructure;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

// Composition root: тут і тільки тут усе зв'язується між собою.
public class Main {

    public static void main(String[] args) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        var io = new ConsoleIO(System.in, out);

        var rooms = Infrastructure.rooms();
        var factory = new BookingFactory(rooms, TouristTaxPolicy.percentage(new BigDecimal("0.01")));
        var registrar = new BookingRegistrar(
                Infrastructure.bookingsFile(Path.of("bookings.dat")),
                Infrastructure.smsNotifier(out));
        var bookings = new BookingController(factory, registrar);

        new ConsoleUi(io, List.of(
                ConsoleCommand.bookRoom(io, bookings),
                ConsoleCommand.listRooms(io, new RoomQueryController(rooms)),
                ConsoleCommand.exit(io))).run();
    }
}

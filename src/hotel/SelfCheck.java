package hotel;

import hotel.application.BookingController;
import hotel.application.BookingFactory;
import hotel.application.BookingRegistrar;
import hotel.application.BookingRequest;
import hotel.application.BookingResult;
import hotel.application.NotificationSender;
import hotel.application.RoomQueryController;
import hotel.cli.ConsoleCommand;
import hotel.cli.ConsoleIO;
import hotel.cli.ConsoleUi;
import hotel.domain.AdditionalService;
import hotel.domain.Booking;
import hotel.domain.BookingRepository;
import hotel.domain.Guest;
import hotel.domain.Room;
import hotel.domain.SeasonPolicy;
import hotel.domain.StayPricing;
import hotel.domain.TouristTaxPolicy;
import hotel.infrastructure.Infrastructure;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Прості самоперевірки без зовнішніх бібліотек. Запуск: java -cp bin hotel.SelfCheck */
public final class SelfCheck {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        // Студент А: домен і поліморфізм
        legacyAndNewGiveSameNumbers();
        unknownSeasonIsRejected();
        bookingCannotBeConfirmedTwice();
        servicesListIsDefensivelyCopied();
        // Студент Б: контролер, сховище, консоль
        invalidInputIsRejected();
        fileRepositoryWritesLine();
        controllerDoesNotTouchInfrastructure();
        consoleEndToEnd();
        System.out.println("Пройшло: " + passed + ", впало: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- допоміжне ----------
    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("OK    " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    private static boolean throwsIllegal(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException | IllegalStateException e) {
            return true;
        }
    }

    private static final class FakeRepo implements BookingRepository {
        final List<Booking> saved = new ArrayList<>();

        @Override
        public void save(Booking booking) {
            saved.add(booking);
        }
    }

    private static final class FakeSender implements NotificationSender {
        final List<String> sent = new ArrayList<>();

        @Override
        public void sendBookingConfirmation(String phone, long bookingId, BigDecimal total) {
            sent.add(phone + "|" + bookingId + "|" + total.toPlainString());
        }
    }

    private static BookingFactory factory() {
        return new BookingFactory(Infrastructure.rooms(), TouristTaxPolicy.percentage(new BigDecimal("0.01")));
    }

    private static BookingController controller(BookingRepository repo, NotificationSender sender) {
        return new BookingController(factory(), new BookingRegistrar(repo, sender));
    }

    private static BookingRequest request(String season) {
        return new BookingRequest("Іван Петренко", "+380971112233", "AA123456", 302, 3, season, true, true);
    }

    // =====================================================================
    // Студент А
    // =====================================================================

    /** Характеризаційний тест: нова версія має давати ті самі суми, що й legacy. */
    private static void legacyAndNewGiveSameNumbers() {
        for (String season : new String[] {"Low", "High", "Peak"}) {
            List<BigDecimal> old = runLegacy(season);
            BookingResult now = controller(new FakeRepo(), new FakeSender()).book(request(season));
            check("legacy == new, сезон " + season,
                    old.size() == 4
                            && old.get(0).compareTo(now.roomTotal()) == 0
                            && old.get(1).compareTo(now.servicesTotal()) == 0
                            && old.get(2).compareTo(now.touristTax()) == 0
                            && old.get(3).compareTo(now.grandTotal()) == 0);
        }
    }

    private static List<BigDecimal> runLegacy(String season) {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(("Іван Петренко\n" + season + "\n").getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            new legacy.LegacyHotelManager().createBookingFromConsole();
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        List<BigDecimal> numbers = new ArrayList<>();
        Matcher m = Pattern.compile("(?:Проживання|Послуги|Туристичний збір|Всього): ([0-9.]+) грн")
                .matcher(buffer.toString(StandardCharsets.UTF_8));
        while (m.find()) {
            numbers.add(new BigDecimal(m.group(1)));
        }
        return numbers;
    }

    private static void unknownSeasonIsRejected() {
        check("невідомий сезон відхиляється",
                throwsIllegal(() -> controller(new FakeRepo(), new FakeSender()).book(request("Winter"))));
    }

    private static void bookingCannotBeConfirmedTwice() {
        Room room = new Room(1, Room.Category.STANDARD, new BigDecimal("1000"));
        StayPricing pricing = StayPricing.of(1, SeasonPolicy.byName("Low"), List.of(),
                TouristTaxPolicy.percentage(new BigDecimal("0.01")));
        Booking booking = Booking.create(1, Guest.register(1, "Тест", "+380971112233", "AB1"), room, pricing);
        booking.confirm();
        check("повторне підтвердження кидає виняток", throwsIllegal(booking::confirm));
        check("статус Confirmed", booking.statusLabel().equals("Confirmed"));
    }

    private static void servicesListIsDefensivelyCopied() {
        List<AdditionalService> list = new ArrayList<>();
        list.add(AdditionalService.breakfast());
        StayPricing pricing = StayPricing.of(2, SeasonPolicy.byName("Low"), list,
                TouristTaxPolicy.percentage(new BigDecimal("0.01")));
        list.add(AdditionalService.parking());
        check("зовнішній список не змінює StayPricing", pricing.servicesTotal().compareTo(new BigDecimal("600")) == 0);
    }

    // =====================================================================
    // Студент Б
    // =====================================================================

    private static void invalidInputIsRejected() {
        BookingController c = controller(new FakeRepo(), new FakeSender());
        check("0 ночей відхиляється", throwsIllegal(() -> c.book(new BookingRequest(
                "Іван", "+380971112233", "AA1", 302, 0, "High", false, false))));
        check("порожнє ПІБ відхиляється", throwsIllegal(() -> c.book(new BookingRequest(
                " ", "+380971112233", "AA1", 302, 1, "High", false, false))));
        check("неіснуючий номер відхиляється", throwsIllegal(() -> c.book(new BookingRequest(
                "Іван", "+380971112233", "AA1", 999, 1, "High", false, false))));
        check("поганий телефон відхиляється", throwsIllegal(() -> c.book(new BookingRequest(
                "Іван", "12", "AA1", 302, 1, "High", false, false))));
    }

    private static void fileRepositoryWritesLine() throws Exception {
        Path file = Files.createTempFile("bookings", ".dat");
        controller(Infrastructure.bookingsFile(file), new FakeSender()).book(request("High"));
        String content = Files.readString(file, StandardCharsets.UTF_8);
        Files.deleteIfExists(file);
        check("файл містить запис про бронювання", content.startsWith("501;Іван Петренко;+380971112233;302;3;8938.50;Confirmed"));
    }

    private static void controllerDoesNotTouchInfrastructure() {
        FakeRepo repo = new FakeRepo();
        FakeSender sender = new FakeSender();
        controller(repo, sender).book(request("High"));
        check("контролер зберіг бронювання через інтерфейс", repo.saved.size() == 1);
        check("контролер надіслав повідомлення через інтерфейс", sender.sent.size() == 1
                && sender.sent.get(0).equals("+380971112233|501|8938.50"));
    }

    private static void consoleEndToEnd() {
        String input = "1\nІван Петренко\n+380971112233\nAA123456\n302\n3\nHigh\ny\ny\n2\nх\n0\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        ConsoleIO io = new ConsoleIO(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), out);
        var rooms = Infrastructure.rooms();
        var bc = new BookingController(
                new BookingFactory(rooms, TouristTaxPolicy.percentage(new BigDecimal("0.01"))),
                new BookingRegistrar(new FakeRepo(), new FakeSender()));
        new ConsoleUi(io, List.of(
                ConsoleCommand.bookRoom(io, bc),
                ConsoleCommand.listRooms(io, new RoomQueryController(rooms)),
                ConsoleCommand.exit(io))).run();
        String text = buffer.toString(StandardCharsets.UTF_8);
        check("CLI: квитанція містить Всього: 8938.50 грн", text.contains("Всього: 8938.50 грн"));
        check("CLI: показує список номерів", text.contains("302  Deluxe  2000 грн/доба"));
        check("CLI: невідомий пункт меню не ламає програму", text.contains("Невідомий пункт меню"));
        check("CLI: вихід працює", text.contains("До побачення!"));
    }
}

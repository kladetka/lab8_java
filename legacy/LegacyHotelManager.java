package legacy;

import java.util.Scanner;

// Опис предметної області: Готельний комплекс та система бронювання номерів.
// Вихідний "поганий" код. Допоміжні класи лежать у цьому ж файлі, щоб він компілювався
// (у Java в одному файлі може бути тільки один public клас).
public class LegacyHotelManager {
    public void createBookingFromConsole() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Бронювання номера в готелі ===");
        System.out.print("Введіть ПІБ гостя: ");
        String name = scanner.nextLine();
        System.out.print("Введіть сезон (Low/High/Peak): ");
        String season = scanner.nextLine();

        Guest guest = new Guest();
        guest.id = 10; guest.fullName = name; guest.phone = "+380971112233"; guest.passportNumber = "AA123456";

        Room room = new Room();
        room.roomNumber = 302; room.category = "Deluxe"; room.basePricePerNight = 2000.0;

        Booking booking = new Booking();
        booking.bookingId = 501;
        booking.guest = guest;
        booking.room = room;
        booking.nights = 3;
        booking.season = season;
        booking.includeBreakfast = true;
        booking.includeParking = true;
        booking.status = "Created";

        double seasonalMultiplier = 1.0;
        if ("Peak".equalsIgnoreCase(booking.season)) {
            seasonalMultiplier = 1.5;
        } else if ("High".equalsIgnoreCase(booking.season)) {
            seasonalMultiplier = 1.25;
        } else if ("Low".equalsIgnoreCase(booking.season)) {
            seasonalMultiplier = 0.9;
        }

        booking.roomTotal = booking.room.basePricePerNight * booking.nights * seasonalMultiplier;

        double services = 0.0;
        if (booking.includeBreakfast) {
            services += 300.0 * booking.nights;
        }
        if (booking.includeParking) {
            services += 150.0 * booking.nights;
        }
        booking.servicesTotal = services;

        booking.touristTax = (booking.roomTotal + booking.servicesTotal) * 0.01;
        booking.grandTotal = booking.roomTotal + booking.servicesTotal + booking.touristTax;
        booking.status = "Confirmed";

        String confirmation = "Бронювання #" + booking.bookingId + " для " + booking.guest.fullName +
                "\nКімната: " + booking.room.roomNumber + " (" + booking.room.category + ")" +
                "\nПроживання: " + booking.roomTotal + " грн" +
                "\nПослуги: " + booking.servicesTotal + " грн" +
                "\nТуристичний збір: " + booking.touristTax + " грн" +
                "\nВсього: " + booking.grandTotal + " грн";
        System.out.println(confirmation);

        System.out.println("[Файлова БД] Запис про бронювання #" + booking.bookingId + " додано до bookings.dat.");
        System.out.println("[SMS] Підтвердження надіслано на номер " + booking.guest.phone);
    }
}

class Guest {
    public int id;
    public String fullName;
    public String phone;
    public String passportNumber;
}

class Room {
    public int roomNumber;
    public String category; // "Standard", "Deluxe", "Suite"
    public double basePricePerNight;
}

class Booking {
    public int bookingId;
    public Guest guest;
    public Room room;
    public int nights;
    public String season; // "Low", "High", "Peak"
    public boolean includeBreakfast;
    public boolean includeParking;
    public double roomTotal;
    public double servicesTotal;
    public double touristTax;
    public double grandTotal;
    public String status;
}

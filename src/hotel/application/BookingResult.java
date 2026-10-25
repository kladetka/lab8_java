package hotel.application;

import hotel.domain.Booking;
import java.math.BigDecimal;

// DTO відповіді. CLI бачить тільки його і про доменні класи нічого не знає.
public record BookingResult(
        long bookingId,
        String guestName,
        String guestPhone,
        int roomNumber,
        String roomCategory,
        BigDecimal roomTotal,
        BigDecimal servicesTotal,
        BigDecimal touristTax,
        BigDecimal grandTotal,
        String status) {

    public static BookingResult from(Booking b) {
        return new BookingResult(
                b.id(),
                b.guestName(),
                b.guestPhone(),
                b.roomNumber(),
                b.roomCategory(),
                b.roomTotal(),
                b.servicesTotal(),
                b.touristTax(),
                b.grandTotal(),
                b.statusLabel());
    }
}

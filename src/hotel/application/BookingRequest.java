package hotel.application;

// DTO запиту. Нічого не рахує, просто несе дані з консолі в контролер.
public record BookingRequest(
        String guestName,
        String phone,
        String passportNumber,
        int roomNumber,
        int nights,
        String season,
        boolean breakfast,
        boolean parking) {
}

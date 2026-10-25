package hotel.application;

import hotel.domain.Booking;
import hotel.domain.BookingRepository;

// Pure Fabrication: підтвердити, зберегти, повідомити гостя. Правил цін тут немає.
public class BookingRegistrar {
    private final BookingRepository repository;
    private final NotificationSender notifier;

    public BookingRegistrar(BookingRepository repository, NotificationSender notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }

    public BookingResult register(Booking booking) {
        booking.confirm();
        repository.save(booking);
        notifier.sendBookingConfirmation(booking.guestPhone(), booking.id(), booking.grandTotal());
        return BookingResult.from(booking);
    }
}

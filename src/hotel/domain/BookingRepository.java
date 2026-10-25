package hotel.domain;

/** Indirection: домен знає тільки цей інтерфейс, а не файл чи БД. */
public interface BookingRepository {
    void save(Booking booking);
}

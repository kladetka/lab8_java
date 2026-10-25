package hotel.domain;

import java.math.BigDecimal;

/**
 * Агрегат бронювання. Стан закритий: змінити його можна тільки через confirm().
 * Суми Booking сам не рахує, а питає у StayPricing, бо той тримає сезон, послуги і збір.
 */
public final class Booking {

    private enum Status {
        CREATED("Created"),
        CONFIRMED("Confirmed");

        private final String label;

        Status(String label) {
            this.label = label;
        }
    }

    private final long id;
    private final Guest guest;
    private final Room room;
    private final StayPricing pricing;
    private Status status;

    private Booking(long id, Guest guest, Room room, StayPricing pricing) {
        this.id = id;
        this.guest = guest;
        this.room = room;
        this.pricing = pricing;
        this.status = Status.CREATED;
    }

    public static Booking create(long id, Guest guest, Room room, StayPricing pricing) {
        if (guest == null || room == null || pricing == null) {
            throw new IllegalArgumentException("Гість, номер і умови проживання обов'язкові");
        }
        return new Booking(id, guest, room, pricing);
    }

    public BigDecimal roomTotal() {
        return this.pricing.roomTotal(this.room);
    }

    public BigDecimal servicesTotal() {
        return this.pricing.servicesTotal();
    }

    public BigDecimal touristTax() {
        return this.pricing.touristTax(this.room);
    }

    public BigDecimal grandTotal() {
        return this.pricing.grandTotal(this.room);
    }

    public void confirm() {
        if (this.status != Status.CREATED) {
            throw new IllegalStateException("Бронювання #" + this.id + " вже підтверджене");
        }
        if (this.grandTotal().signum() <= 0) {
            throw new IllegalStateException("Не можна підтвердити бронювання з нульовою сумою");
        }
        this.status = Status.CONFIRMED;
    }

    // Делегуючі методи, щоб зовнішній код не ходив ланцюжками (Закон Деметри).
    public long id() {
        return this.id;
    }

    public String guestName() {
        return this.guest.fullName();
    }

    public String guestPhone() {
        return this.guest.phone();
    }

    public int roomNumber() {
        return this.room.number();
    }

    public String roomCategory() {
        return this.room.categoryLabel();
    }

    public int nights() {
        return this.pricing.nights();
    }

    public String statusLabel() {
        return this.status.label;
    }
}

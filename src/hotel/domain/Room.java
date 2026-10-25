package hotel.domain;

import java.math.BigDecimal;

/**
 * Номер готелю. Сам знає, скільки коштують його ночі (Information Expert).
 */
public final class Room {

    /** Категорія номера. У legacy це був String, тепер помилитись у написанні не вийде. */
    public enum Category {
        STANDARD("Standard"),
        DELUXE("Deluxe"),
        SUITE("Suite");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String label() {
            return this.label;
        }
    }

    private final int roomNumber;
    private final Category category;
    private final BigDecimal basePricePerNight;

    public Room(int roomNumber, Category category, BigDecimal basePricePerNight) {
        if (roomNumber <= 0) {
            throw new IllegalArgumentException("Номер кімнати має бути додатним");
        }
        if (category == null) {
            throw new IllegalArgumentException("Категорія номера обов'язкова");
        }
        if (basePricePerNight == null || basePricePerNight.signum() <= 0) {
            throw new IllegalArgumentException("Ціна за ніч має бути додатною");
        }
        this.roomNumber = roomNumber;
        this.category = category;
        this.basePricePerNight = basePricePerNight;
    }

    /** Базова вартість проживання без сезонної націнки. */
    public BigDecimal costFor(int nights) {
        return this.basePricePerNight.multiply(BigDecimal.valueOf(nights));
    }

    public int number() {
        return this.roomNumber;
    }

    public String categoryLabel() {
        return this.category.label();
    }

    public BigDecimal pricePerNight() {
        return this.basePricePerNight;
    }
}

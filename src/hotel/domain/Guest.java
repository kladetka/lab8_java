package hotel.domain;

/**
 * Гість. Створюється тільки через register(), тобто завжди у валідному стані (Creator).
 */
public final class Guest {
    private final long id;
    private final String fullName;
    private final String phone;
    private final String passportNumber;

    private Guest(long id, String fullName, String phone, String passportNumber) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.passportNumber = passportNumber;
    }

    public static Guest register(long id, String fullName, String phone, String passportNumber) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("ПІБ гостя не може бути порожнім");
        }
        if (phone == null || !phone.matches("\\+?[0-9]{10,13}")) {
            throw new IllegalArgumentException("Некоректний номер телефону: " + phone);
        }
        if (passportNumber == null || passportNumber.isBlank()) {
            throw new IllegalArgumentException("Номер паспорта обов'язковий");
        }
        return new Guest(id, fullName.trim(), phone.trim(), passportNumber.trim());
    }

    public long id() {
        return this.id;
    }

    public String fullName() {
        return this.fullName;
    }

    public String phone() {
        return this.phone;
    }

    public String passportNumber() {
        return this.passportNumber;
    }
}

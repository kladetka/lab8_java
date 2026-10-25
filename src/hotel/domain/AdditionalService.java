package hotel.domain;

import java.math.BigDecimal;

/**
 * Додаткова послуга (сніданок, паркування, ...). Нова послуга додається без зміни
 * Booking і StayPricing, бо вони працюють тільки з цим інтерфейсом (Polymorphism).
 */
public interface AdditionalService {

    String name();

    BigDecimal cost(int nights);

    static AdditionalService breakfast() {
        return new BreakfastService();
    }

    static AdditionalService parking() {
        return new ParkingService();
    }
}

final class BreakfastService implements AdditionalService {
    private static final BigDecimal PRICE_PER_NIGHT = new BigDecimal("300");

    @Override
    public String name() {
        return "Сніданок";
    }

    @Override
    public BigDecimal cost(int nights) {
        return PRICE_PER_NIGHT.multiply(BigDecimal.valueOf(nights));
    }
}

final class ParkingService implements AdditionalService {
    private static final BigDecimal PRICE_PER_NIGHT = new BigDecimal("150");

    @Override
    public String name() {
        return "Паркування";
    }

    @Override
    public BigDecimal cost(int nights) {
        return PRICE_PER_NIGHT.multiply(BigDecimal.valueOf(nights));
    }
}

package hotel.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Умови проживання і розрахунок вартості (Information Expert): тут лежать ночі,
 * сезон, послуги і політика збору, тому саме тут їх і рахуємо.
 */
public final class StayPricing {
    private final int nights;
    private final SeasonPolicy season;
    private final List<AdditionalService> services;
    private final TouristTaxPolicy taxPolicy;

    private StayPricing(int nights, SeasonPolicy season, List<AdditionalService> services,
                        TouristTaxPolicy taxPolicy) {
        this.nights = nights;
        this.season = season;
        this.services = services;
        this.taxPolicy = taxPolicy;
    }

    public static StayPricing of(int nights, SeasonPolicy season, List<AdditionalService> services,
                                 TouristTaxPolicy taxPolicy) {
        if (nights <= 0) {
            throw new IllegalArgumentException("Кількість ночей має бути більше нуля");
        }
        if (season == null || services == null || taxPolicy == null) {
            throw new IllegalArgumentException("Сезон, послуги і політика збору обов'язкові");
        }
        // копія списку, щоб зовнішній код не міг змінити стан після створення
        return new StayPricing(nights, season, List.copyOf(services), taxPolicy);
    }

    public int nights() {
        return this.nights;
    }

    public BigDecimal roomTotal(Room room) {
        return this.season.apply(room.costFor(this.nights)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal servicesTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (AdditionalService service : this.services) {
            sum = sum.add(service.cost(this.nights));
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal touristTax(Room room) {
        return this.taxPolicy.taxOn(this.roomTotal(room).add(this.servicesTotal()));
    }

    public BigDecimal grandTotal(Room room) {
        return this.roomTotal(room).add(this.servicesTotal()).add(this.touristTax(room));
    }
}

package hotel.domain;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

/**
 * Стабільний контракт для сезонної націнки (Protected Variations).
 * Замість каскаду if-else по назві сезону: поліморфні реалізації + словник (Dictionary Dispatch).
 */
public interface SeasonPolicy {

    String name();

    BigDecimal apply(BigDecimal baseAmount);

    /** Повертає політику за назвою сезону. Невідома назва це помилка, а не множник 1.0, як у legacy. */
    static SeasonPolicy byName(String name) {
        SeasonPolicy policy = name == null ? null : SeasonPolicies.BY_NAME.get(name.trim().toLowerCase(Locale.ROOT));
        if (policy == null) {
            throw new IllegalArgumentException("Невідомий сезон: " + name + " (Low/High/Peak)");
        }
        return policy;
    }
}

/** Таблиця сезонів. Новий сезон = новий клас нижче і один рядок тут. */
final class SeasonPolicies {
    static final Map<String, SeasonPolicy> BY_NAME = Map.of(
            "low", new LowSeasonPolicy(),
            "high", new HighSeasonPolicy(),
            "peak", new PeakSeasonPolicy());

    private SeasonPolicies() {
    }
}

final class LowSeasonPolicy implements SeasonPolicy {
    private static final BigDecimal MULTIPLIER = new BigDecimal("0.90");

    @Override
    public String name() {
        return "Low";
    }

    @Override
    public BigDecimal apply(BigDecimal baseAmount) {
        return baseAmount.multiply(MULTIPLIER);
    }
}

final class HighSeasonPolicy implements SeasonPolicy {
    private static final BigDecimal MULTIPLIER = new BigDecimal("1.25");

    @Override
    public String name() {
        return "High";
    }

    @Override
    public BigDecimal apply(BigDecimal baseAmount) {
        return baseAmount.multiply(MULTIPLIER);
    }
}

final class PeakSeasonPolicy implements SeasonPolicy {
    private static final BigDecimal MULTIPLIER = new BigDecimal("1.50");

    @Override
    public String name() {
        return "Peak";
    }

    @Override
    public BigDecimal apply(BigDecimal baseAmount) {
        return baseAmount.multiply(MULTIPLIER);
    }
}

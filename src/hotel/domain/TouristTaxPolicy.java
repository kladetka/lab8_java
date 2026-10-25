package hotel.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Туристичний збір може змінитись законодавчо, тому схований за інтерфейсом
 * (Protected Variations).
 */
public interface TouristTaxPolicy {

    BigDecimal taxOn(BigDecimal taxableAmount);

    /** Збір у відсотках від суми, наприклад 0.01 це 1%. */
    static TouristTaxPolicy percentage(BigDecimal rate) {
        return new PercentageTouristTax(rate);
    }
}

final class PercentageTouristTax implements TouristTaxPolicy {
    private final BigDecimal rate;

    PercentageTouristTax(BigDecimal rate) {
        if (rate == null || rate.signum() < 0) {
            throw new IllegalArgumentException("Ставка збору не може бути від'ємною");
        }
        this.rate = rate;
    }

    @Override
    public BigDecimal taxOn(BigDecimal taxableAmount) {
        return taxableAmount.multiply(this.rate).setScale(2, RoundingMode.HALF_UP);
    }
}

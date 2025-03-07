package dev.kmfg.flooring.service.validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class GenericValidator {
    public static final NumberFormat NICE_NUMBER_FORMATTER = NumberFormat.getNumberInstance(Locale.US);
    public static final DateTimeFormatter STR_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    public static final DateTimeFormatter EXPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    static {
        NICE_NUMBER_FORMATTER.setMinimumFractionDigits(2);
        NICE_NUMBER_FORMATTER.setMaximumFractionDigits(2);
    }

    private static final int BIG_DECIMAL_SCALE = 2;
    private static final RoundingMode BIG_DECIMAL_ROUNDING = RoundingMode.HALF_UP;

    /**
     * Create a big decimal to the current order spec.
     * @param bdStr the number
     * @return the number as a big decimal.
     */
    public static BigDecimal createBigDecimal(String bdStr) {
        return new BigDecimal(bdStr).setScale(BIG_DECIMAL_SCALE, BIG_DECIMAL_ROUNDING);
    }
}

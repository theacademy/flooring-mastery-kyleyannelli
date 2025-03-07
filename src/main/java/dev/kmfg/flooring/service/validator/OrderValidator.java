package dev.kmfg.flooring.service.validator;

import java.math.BigDecimal;

public class OrderValidator {
    // IDs
    public static int MIN_ORDER_NUMBER = 1;
    public static int MAX_ORDER_NUMBER = Integer.MAX_VALUE;

    // AREA
    public static final int MIN_BIG_DECIMAL_DIGITS = 3;
    public static final int MAX_BIG_DECIMAL_DIGITS = 7;
    public static final int AREA_SCALE = 2;
    public static final BigDecimal AREA_MIN_BD = GenericValidator.createBigDecimal("100.00");
    public static final BigDecimal AREA_MAX_BD = GenericValidator.createBigDecimal("9999999.99");
    private static final String AREA_REGEX = String.format("^\\d{%d,%d}\\.\\d{%d}$", MIN_BIG_DECIMAL_DIGITS, MAX_BIG_DECIMAL_DIGITS, AREA_SCALE);

    // CUSTOMER NAME
    public static final int MIN_CUSTOMER_NAME_CHARS = 1;
    public static final int MAX_CUSTOMER_NAME_CHARS = 50;
    private static final String VALID_CUSTOMER_NAME_REGEX = String.format("^[a-zA-Z0-9., ]{%d,%d}$", MIN_CUSTOMER_NAME_CHARS, MAX_CUSTOMER_NAME_CHARS);

    /**
     * Checks that the area meets spec.
     * @param area a number to the hundredths place.
     * @return if the area meets spec.
     */
    public static boolean isAreaInvalid(String area) {
        return area == null ||
                !area.matches(AREA_REGEX);
    }

    /**
     * Checks if given name meets spec.
     * @param customerName to check
     * @return if the name meets spec.
     */
    public static boolean isCustomerNameInvalid(String customerName) {
        return customerName == null ||
                customerName.isBlank() ||
                customerName.charAt(0) == ' ' ||
                !customerName.matches(VALID_CUSTOMER_NAME_REGEX);
    }
}

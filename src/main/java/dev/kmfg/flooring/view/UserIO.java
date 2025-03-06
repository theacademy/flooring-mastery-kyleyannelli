package dev.kmfg.flooring.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public interface UserIO {
    void print(String msg);

    /**
     * @param prompt to show the user.
     * @return true if given "y", false otherwise.
     */
    boolean readBoolean(String prompt);

    /**
     * Reads an int until met to [min, max].
     * @param prompt to show the user until a valid int is given.
     * @param min inclusive
     * @param max inclusive
     * @return int in range
     */
    int readInt(String prompt, int min, int max);

    String readString(String prompt);

    /**
     * Reads local date in exact given format.
     * @param prompt to show the user until a valid date is given.
     * @param formatter to match the input to.
     * @return date
     */
    LocalDate readLocalDate(String prompt, DateTimeFormatter formatter);

    /**
     * Reads local date in exact given format.
     * @param prompt to show the user until a valid date is given.
     * @param min date to allow, inclusive.
     * @param formatter to match the input to.
     * @return date that meets or exceeds the minimum.
     */
    LocalDate readLocalDate(String prompt, LocalDate min, DateTimeFormatter formatter);

    /**
     * @param prompt to show the user until they enter a valid big decimal.
     * @param scale to require & set the big decimal to.
     * @param min big decimal that can be entered. Must match the given scale precisely.
     * @return a big decimal at given scale, always rounding half up.
     */
    BigDecimal readBigDecimal(String prompt, int scale, BigDecimal min);
}

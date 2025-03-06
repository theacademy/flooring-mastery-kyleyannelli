package dev.kmfg.flooring.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public interface UserIO {
    void print(String msg);
    boolean readBoolean(String prompt);
    int readInt(String prompt);
    int readInt(String prompt, int min, int max);
    String readString(String prompt);
    String readString(String prompt, int minChars, int maxChars);
    LocalDate readLocalDate(String prompt, DateTimeFormatter formatter);
    LocalDate readLocalDate(String prompt, LocalDate min, DateTimeFormatter formatter);
    BigDecimal readBigDecimal(String prompt);
    BigDecimal readBigDecimal(String prompt, int scale, BigDecimal min);
}

package dev.kmfg.flooring.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UserIOConsoleImpl implements UserIO {
    private static final int MINIMUM_BIG_DECIMAL_DIGITS = 3;
    private static final int MAXIMUM_BIG_DECIMAL_DIGITS = 7;
    private static final String BIG_DECIMAL_REGEX_FORMAT = "^\\d{%d,%d}\\.\\d{%d}$";

    private final Scanner userInput;

    public UserIOConsoleImpl() {
        this.userInput = new Scanner(System.in);
    }

    @Override
    public void print(String msg) {
        System.out.println(msg);
    }

    @Override
    public boolean readBoolean(String prompt) {
        return readString(prompt).equalsIgnoreCase("y");
    }

    @Override
    public int readInt(String prompt) {
        print(prompt);

        final int x = userInput.nextInt();
        userInput.nextLine();
        return x;
    }

    @Override
    public int readInt(String prompt, int min, int max) {
        int x = min == Integer.MIN_VALUE ? min : min - 1;
        while (x < min || x > max) {
            print(prompt);

            try {
                x = userInput.nextInt();
            } catch (InputMismatchException e) {
                print("You can only enter integers.");
            }
            userInput.nextLine();
        }
        return x;
    }

    @Override
    public String readString(String prompt) {
        print(prompt);
        return userInput.nextLine();
    }

    /**
     * Prompts user for a string until provided with one that has [minChars, maxChars] characters.
     * @param prompt to display to the user.
     * @param minChars should be 1 or greater
     * @param maxChars should be greater than minChars
     * @return String that meets min, max requirements.
     */
    @Override
    public String readString(String prompt, int minChars, int maxChars) {
        print(prompt);
        String s = "";
        while(s.length() < minChars || s.length() > maxChars) {
            s = userInput.nextLine();
        }
        return s;
    }

    @Override
    public LocalDate readLocalDate(String prompt) {
        return null;
    }

    @Override
    public LocalDate readLocalDate(String prompt, LocalDate min, DateTimeFormatter formatter) {
        Scanner scanner = new Scanner(System.in);
        LocalDate date = null;

        while(date == null) {
            print(prompt);
            String input = scanner.nextLine().trim();

            try {
                date = LocalDate.parse(input, formatter);

                if(date.isBefore(min)) {
                    print(
                            String.format(
                                    "Date must not be before %s",
                                    min.format(formatter)
                            )
                    );
                    date = null;
                }
            } catch(DateTimeParseException e) {
                print("Invalid date format. Please use MM/DD/YYYY format.");
            }
        }

        return date;
    }

    @Override
    public BigDecimal readBigDecimal(String prompt) {
        return null;
    }

    @Override
    public BigDecimal readBigDecimal(String prompt, int scale, BigDecimal min) {
        String s = "";
        BigDecimal givenBigDecimal = min.subtract(BigDecimal.ONE).setScale(scale, RoundingMode.HALF_UP);

        final String regex = String.format(
                BIG_DECIMAL_REGEX_FORMAT,
                MINIMUM_BIG_DECIMAL_DIGITS,
                MAXIMUM_BIG_DECIMAL_DIGITS,
                scale
        );
        boolean givenBigDecimalMeetsMin = false;
        while(!s.matches(regex) || !givenBigDecimalMeetsMin) {
            print(prompt);
            s = userInput.nextLine();

            if(s.matches(regex)) {
                givenBigDecimal = new BigDecimal(s).setScale(scale, RoundingMode.HALF_UP);
                givenBigDecimalMeetsMin = givenBigDecimal.compareTo(min) >= 0;
            }
        }

        return givenBigDecimal;
    }
}

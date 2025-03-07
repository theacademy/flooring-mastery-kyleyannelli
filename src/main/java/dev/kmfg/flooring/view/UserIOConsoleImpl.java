package dev.kmfg.flooring.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class UserIOConsoleImpl implements UserIO {
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

    /**
     * Get an int within the requested range. The minimum will overflow if Integer.MIN_VALUE is given for min.
     * Additionally, this will not check if you give the max as the min, and min as the max.
     * @param prompt to show the user until a valid int is given.
     * @param min inclusive
     * @param max inclusive
     * @return int within the inclusive range.
     */
    @Override
    public int readInt(String prompt, int min, int max) {
        int x = min - 1;
        // while x is outside the range.
        while(x < min || x > max) {
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

    @Override
    public LocalDate readLocalDate(String prompt, DateTimeFormatter formatter) {
        Scanner scanner = new Scanner(System.in);
        LocalDate date = null;

        while(date == null) {
            print(prompt);
            String input = scanner.nextLine().trim();

            try {
                date = LocalDate.parse(input, formatter);
            } catch(DateTimeParseException e) {
                print("Invalid date format. Please use MM/DD/YYYY format.");
            }
        }

        return date;
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
}

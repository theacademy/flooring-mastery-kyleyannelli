package dev.kmfg.flooring.test;

import dev.kmfg.flooring.service.validator.OrderValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

public class OrderValidatorTest {
    @Test
    public void testCustomerNameValidation() {
        // =======             =======
        // ======= VALID NAMES
        // =======             =======
        StringBuilder maxNameBuilder = new StringBuilder();
        // intellij suggested this over the for loop I originally had
        maxNameBuilder.append("a".repeat(OrderValidator.MAX_CUSTOMER_NAME_CHARS));
        // test valid names
        Stream.of(
                "ABC, Inc.",
                // min chars req
                "A",
                // max chars (currently 50)
                maxNameBuilder.toString()
        ).forEach(validName -> assertFalse(
                OrderValidator.isCustomerNameInvalid(validName),
                String.format(
                        "%s of length %d should be a valid name!",
                        validName,
                        validName.length()
                )
        ));

        // =======             =======
        // ======= INVALID NAMES
        // =======             =======
        final int ASCII_CHAR_START = 33;
        final int ASCII_CHAR_END = 126;
        final int ASCII_UPPERCASE_START = 65;
        final int ASCII_UPPERCASE_END = 90;
        final int ASCII_LOWERCASE_START = 97;
        final int ASCII_LOWERCASE_END = 122;
        for(int i = ASCII_CHAR_START; i <= ASCII_CHAR_END; i++) {
            // chars also cast to int ASCII value
            // Pretty sure java does this automatically,
            //  the explicit cast may make it more clear.
            // if we are {a-zA-Z0-9,. } (valid according to spec, skip)
            if(
                    (i == (int) ',' || i == (int) '.') ||
                    (i >= '0' && i <= '9') ||
                    (i >= ASCII_UPPERCASE_START && i <= ASCII_UPPERCASE_END) ||
                    (i >= ASCII_LOWERCASE_START && i <= ASCII_LOWERCASE_END)
            )
            {
                continue;
            }

            // java casts int values to their respective ASCII char value
            // here it is not automatically cast to a char
            final String checkChar = "" + (char) i;
            assertTrue(
                    OrderValidator.isCustomerNameInvalid(checkChar),
                    checkChar + " is supposed to be invalid!"
            );
        }
        // null should be invalid
        assertTrue(OrderValidator.isCustomerNameInvalid(null));
        // empty string should be invalid
        assertTrue(OrderValidator.isCustomerNameInvalid(""));
        // it cannot be blank
        assertTrue(OrderValidator.isCustomerNameInvalid("         "));
        // it cannot start with whitespace
        assertTrue(OrderValidator.isCustomerNameInvalid("         ABC"));
    }

    @Test
    public void testAreaValidation() {
        // =======             =======
        // ======= VALID AREAS
        // =======             =======
        assertFalse(OrderValidator.isAreaInvalid("100.00")); // current min
        assertFalse(OrderValidator.isAreaInvalid("200.00")); // random valid
        assertFalse(OrderValidator.isAreaInvalid("61273.49")); // random middle num
        assertFalse(OrderValidator.isAreaInvalid("9999999.99")); // current max

        // =======             =======
        // ======= INVALID AREAS
        // =======             =======
        assertTrue(OrderValidator.isAreaInvalid(null));
        assertTrue(OrderValidator.isAreaInvalid(""));
        assertTrue(OrderValidator.isAreaInvalid("99.99")); // right before min
        assertTrue(OrderValidator.isAreaInvalid("10000000.00")); // right after max
        assertTrue(OrderValidator.isAreaInvalid("100")); // the minimum, but incorrect scale
        assertTrue(OrderValidator.isAreaInvalid("100.0")); // the minimum, but incorrect scale
        assertTrue(OrderValidator.isAreaInvalid("100.000")); // the minimum, but incorrect scale
        assertTrue(OrderValidator.isAreaInvalid("9999999")); // the maximum, but incorrect scale
        assertTrue(OrderValidator.isAreaInvalid("9999999.9")); // the maximum, but incorrect scale
        assertTrue(OrderValidator.isAreaInvalid("9999999.999")); // the maximum, but incorrect scale
        assertTrue(OrderValidator.isAreaInvalid(" 200.00")); // valid with start whitespace
        assertTrue(OrderValidator.isAreaInvalid("200.00 ")); // valid with end whitespace
        assertTrue(OrderValidator.isAreaInvalid(" 200.00 ")); // valid with padded whitespace
    }
}

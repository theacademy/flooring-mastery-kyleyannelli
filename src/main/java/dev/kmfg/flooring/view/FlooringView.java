package dev.kmfg.flooring.view;

import dev.kmfg.flooring.dto.Order;
import dev.kmfg.flooring.dto.Product;
import dev.kmfg.flooring.dto.StateTax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlooringView {
    private static final int MIN_CUSTOMER_NAME_CHARS = 1;
    private static final int MAX_CUSTOMER_NAME_CHARS = 100;
    private static final String VALID_CUSTOMER_NAME_PATTERN = String.format("^[a-zA-Z0-9., ]{%d,%d}$", MIN_CUSTOMER_NAME_CHARS, MAX_CUSTOMER_NAME_CHARS);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100).setScale(2, RoundingMode.HALF_UP);
    private static final DateTimeFormatter STR_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final UserIO io;

    public FlooringView(UserIO io) {
        this.io = io;
    }

    private void displayHeader(String title) {
        if(title == null || title.isBlank()) {
            title = "=== ===";
        }

        io.print(
                String.format(
                        "=== %s ===",
                        title.toUpperCase()
                )
        );
    }

    private LocalDate promptOrderDate(LocalDate min) {
        return io.readLocalDate(
                String.format(
                        "Enter a date in MM/DD/YYYY format. The date must be %s or later.",
                        min.format(STR_DATE_FORMATTER)
                ),
                min,
                STR_DATE_FORMATTER
        );
    }

    private LocalDate promptOrderDate() {
        return io.readLocalDate("Enter a date in MM/DD/YYYY format.");
    }

    private int promptOrderNumber() {
        return io.readInt("Enter the order number.", 1, Integer.MAX_VALUE);
    }

    private String promptCustomerName() {
        String givenCustomerName = "";

        while(!givenCustomerName.matches(VALID_CUSTOMER_NAME_PATTERN)) {
            givenCustomerName = io.readString(
                    String.format(
                            "Provide a customer name. It must be [%d, %d] characters. It can only contain a-Z, 0-9, period, or comma characters.",
                            MIN_CUSTOMER_NAME_CHARS,
                            MAX_CUSTOMER_NAME_CHARS
                    )
            );
        }

        return givenCustomerName;
    }

    private BigDecimal promptBigDecimal() {
        return io.readBigDecimal(
                "Enter a value in XXXX.XX format. It must be exactly to the hundredths place.",
                2,
                BigDecimal.ONE
        );
    }

    private BigDecimal promptBigDecimal(String prompt, BigDecimal min) {
        return io.readBigDecimal(
                prompt + " Enter a value in XXXX.XX format. It must be exactly to the hundredths place.\n\tand at least " + min.toString(),
                2,
                min
        );
    }

    private BigDecimal promptBigDecimal(BigDecimal min) {
        return io.readBigDecimal(
                "Enter a value in XXXX.XX format. It must be exactly to the hundredths place.\n\tand at least " + min.toString(),
                2,
                min
        );
    }

    private StateTax promptStateTax(List<StateTax> stateTaxes) {
        StateTax pickedStateTax = null;
        while(pickedStateTax == null) {
            final String givenStateAbbreviation = io.readString("Enter an available state abbreviation.");
            pickedStateTax = stateTaxes.stream()
                    .filter(stateTax -> stateTax.getStateAbbreviation().equalsIgnoreCase(givenStateAbbreviation))
                    .findFirst()
                    .orElse(null);
        }
        return pickedStateTax;
    }

    private void displayStateTaxes(List<StateTax> stateTaxes) {
        displayHeader("Available States");
        for(StateTax stateTax : stateTaxes) {
            io.print(stateTax.toString());
        }
    }

    private Product promptProduct(List<Product> products) {
        Product pickedProduct = null;
        while(pickedProduct == null) {
            final String givenProductType = io.readString("Enter an available product type.");
            pickedProduct = products.stream()
                    .filter(product -> product.getProductType().equalsIgnoreCase(givenProductType))
                    .findFirst()
                    .orElse(null);
        }
        return pickedProduct;
    }

    private void displayProducts(List<Product> products) {
        displayHeader("Available Products");
        for(Product product : products) {
            io.print(product.toString());
        }
    }

    public Order displayAddOrder(List<Product> products, List<StateTax> stateTaxes) {
        displayHeader("");
        displayHeader("Add an Order");
        displayHeader("");

        final LocalDate givenDate = promptOrderDate(LocalDate.now());
        final String givenCustomerName = promptCustomerName();

        displayStateTaxes(stateTaxes);
        final StateTax pickedStateTax = promptStateTax(stateTaxes);

        displayProducts(products);
        final Product pickedProduct = promptProduct(products);

        final BigDecimal area = promptBigDecimal("Enter the area sqft.", ONE_HUNDRED);

        return new Order()
                .setOrderDate(givenDate)
                .setCustomerName(givenCustomerName)
                .setStateTax(pickedStateTax)
                .setProduct(pickedProduct)
                .setArea(area);
    }

    public void displayPressEnterToContinue() {
        io.readString("Press enter to continue...");
    }

    public MenuSelection displayAndGetMenuOption() {
        final MenuSelection[] values = MenuSelection.values();
        displayHeader("Menu");
        for (int i = 1; i < values.length; i++) {
            io.print(i + ". " + values[i].getNiceName());
        }
        return MenuSelection
                .fromInt(io.readInt("Please select an option 1 through " + (values.length - 1), 1, values.length - 1));
    }

    public void displayUnimplementedMenuSelection(MenuSelection selection) {
        displayHeader("Unimplemented Menu Selection");
        io.print(
                String.format(
                        "%s is not implemented in this program.",
                        selection.name()
                )
        );
    }

    public void displayAddedOrder(Order order) {
        io.print(
                String.format(
                        "%s\nThe above order has been added.",
                        order
                )
        );
    }

    public void displayError(Exception e) {
        io.print(
                String.format(
                        "Program could not complete action due to %s.\n\t%s",
                        e.getClass(),
                        e.getMessage()
                )
        );
    }

    public void displayGoodbye() {
        displayHeader("");
        displayHeader("Goodbye!");
        displayHeader("");
    }
}

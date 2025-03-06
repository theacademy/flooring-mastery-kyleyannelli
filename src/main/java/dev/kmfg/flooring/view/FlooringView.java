package dev.kmfg.flooring.view;

import dev.kmfg.flooring.dto.Order;
import dev.kmfg.flooring.dto.Product;
import dev.kmfg.flooring.dto.StateTax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
        return io.readLocalDate("Enter a date in MM/DD/YYYY format.", STR_DATE_FORMATTER);
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

    private BigDecimal promptArea() {
        return io.readBigDecimal(
                String.format(
                        "Enter the area sqft in XXXX.XX format. It must be exactly to the hundredths place. At least %s or greater.",
                        ONE_HUNDRED.toString()
                ),
                2,
                ONE_HUNDRED
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

    public LocalDate displayFindOrders() {
        displayHeader("Find Orders");
        return promptOrderDate();
    }

    public Order displayFindOrder() {
        displayHeader("Find Specific Order");
        return new Order()
                .setOrderDate(promptOrderDate())
                .setOrderNumber(promptOrderNumber());
    }

    public void displayFoundOrders(List<Order> foundOrders, LocalDate ordersDate) {
        if(foundOrders.isEmpty()) {
            io.print(
                    String.format(
                            "No orders found for %s.",
                            ordersDate.format(STR_DATE_FORMATTER)
                    )
            );
        } else {
            io.print(
                    String.format(
                            "Orders found for %s.",
                            ordersDate.format(STR_DATE_FORMATTER)
                    )
            );
            foundOrders.forEach(System.out::println);
        }
    }

    public Optional<Order> displayAddOrder(List<Product> products, List<StateTax> stateTaxes) {
        displayHeader("");
        displayHeader("Add an Order");
        displayHeader("");

        final LocalDate givenDate = promptOrderDate(LocalDate.now());
        final String givenCustomerName = promptCustomerName();

        displayStateTaxes(stateTaxes);
        final StateTax pickedStateTax = promptStateTax(stateTaxes);

        displayProducts(products);
        final Product pickedProduct = promptProduct(products);

        final BigDecimal area = promptArea();

        final Order orderToCreate = new Order()
                .setOrderDate(givenDate)
                .setCustomerName(givenCustomerName)
                .setStateTax(pickedStateTax)
                .setProduct(pickedProduct)
                .setArea(area);

        if(io.readBoolean(String.format("Place order %s\n(y/n)", orderToCreate.toString()))) {
            return Optional.of(orderToCreate);
        }
        io.print("Order not placed.");
        return Optional.empty();
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
                        selection.getNiceName()
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

    public Order displayEditOrder(Order orderToEdit, List<StateTax> stateTaxes, List<Product> products) {
        displayHeader(
                String.format(
                        "Editing Order #%d %s",
                        orderToEdit.getOrderNumber(),
                        orderToEdit.getOrderDate().format(STR_DATE_FORMATTER)
                )
        );

        final Order editedOrder = orderToEdit.cloneOrder();

        if(io.readBoolean("The current customer name is " + editedOrder.getCustomerName() + "\nWould you like to change it? (y/n)")) {
            editedOrder.setCustomerName(promptCustomerName());
        }

        if(io.readBoolean("The current state tax is " + editedOrder.getStateTax().toString() + "\nWould you like to change it? (y/n)")) {
            displayStateTaxes(stateTaxes);
            editedOrder.setStateTax(promptStateTax(stateTaxes));
        }

        if(io.readBoolean("The current product is " + editedOrder.getProduct().toString() + "\nWould you like to change it? (y/n)")) {
            displayProducts(products);
            editedOrder.setProduct(promptProduct(products));
        }

        if(io.readBoolean("The current area is " + editedOrder.getArea().toString() + " sqft.\nWould you like to change it? (y/n)")) {
            editedOrder.setArea(promptArea());
        }

        return editedOrder;
    }

    public void displayEditedOrderNotChanged(Order originalOrder, Order editedOrder) {
        displayHeader("Order Did Not Change");
        io.print(
                String.format(
                        "The order \nORIGINAL:\n\t%s\nEDITED:\n\t%s\nare identical. Therefore no changes have been made.",
                        originalOrder.toString(),
                        editedOrder.toString()
                )
        );
    }

    public boolean displayConfirmOrderRemove(Order orderToRemove) {
        displayHeader("Confirm Order Removal");
        io.print(
                String.format(
                        "\n%s\n\nThis removal is permanent.",
                        orderToRemove.toString()
                )
        );
        final boolean doChange = io.readBoolean("Confirm removal of above order? (y/n)");
        io.print(doChange ? "Order will be removed!" : "Order will not be removed.");
        return doChange;
    }

    public boolean displayConfirmOrderChange(Order originalOrder, Order editedOrder) {
        displayHeader("Confirm Order Edit");
        io.print(
                String.format(
                        "\nORIGINAL:\n\t%s\nEDITED:\n\t%s\n",
                        originalOrder.toString(),
                        editedOrder.toString()
                )
        );
        final boolean doChange = io.readBoolean("Confirm changes? (y/n)");
        io.print(doChange ? "Order will be updated!" : "Order will not be updated.");
        return doChange;
    }

    public void displayOrderNotFound(LocalDate orderDate, int orderNumber) {
        displayHeader("Found Order");
        io.print(
                String.format(
                        "No order found for order #%d on %s.",
                        orderNumber,
                        orderDate.format(STR_DATE_FORMATTER)
                )
        );
    }

    public void displayError(Exception e) {
        String RED = "\u001B[31m";
        String RESET = "\u001B[0m";

        if(e.getCause() == null) {
            io.print(
                    String.format(
                            "%sProgram could not complete action due to %s.\n\t%s%s",
                            RED,
                            e.getClass(),
                            e.getMessage(),
                            RESET
                    )
            );
        } else {
            io.print(
                    String.format(
                            "%sProgram could not complete action due to %s.\n\t%s\n\t%s%s",
                            RED,
                            e.getClass(),
                            e.getMessage(),
                            e.getCause().getMessage(),
                            RESET
                    )
            );
        }
    }

    public void displayGoodbye() {
        displayHeader("");
        displayHeader("Goodbye!");
        displayHeader("");
    }
}

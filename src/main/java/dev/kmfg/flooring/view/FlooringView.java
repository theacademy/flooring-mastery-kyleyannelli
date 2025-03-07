package dev.kmfg.flooring.view;

import dev.kmfg.flooring.model.Order;
import dev.kmfg.flooring.model.Product;
import dev.kmfg.flooring.model.StateTax;
import dev.kmfg.flooring.service.validator.GenericValidator;
import dev.kmfg.flooring.service.validator.OrderValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FlooringView {
    private final UserIO io;

    public FlooringView(UserIO io) {
        this.io = io;
    }

    /**
     * Displays the program header.
     * If title is null or blank, spacers will be added.
     * @param title the title to place in the header.
     */
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
                        min.format(GenericValidator.STR_DATE_FORMATTER)
                ),
                min,
                GenericValidator.STR_DATE_FORMATTER
        );
    }

    private LocalDate promptOrderDate() {
        return io.readLocalDate("Enter a date in MM/DD/YYYY format.", GenericValidator.STR_DATE_FORMATTER);
    }

    private int promptOrderNumber() {
        return io.readInt("Enter the order number.", OrderValidator.MIN_ORDER_NUMBER, OrderValidator.MAX_ORDER_NUMBER);
    }

    /**
     * @return customer name validated by regex
     */
    private String promptCustomerName() {
        String givenCustomerName = "";

        while(OrderValidator.isCustomerNameInvalid(givenCustomerName)) {
            givenCustomerName = io.readString(
                    String.format(
                            "Provide a customer name. It must be [%d, %d] characters. It can only contain a-Z, 0-9, period, or comma characters.",
                            OrderValidator.MIN_CUSTOMER_NAME_CHARS,
                            OrderValidator.MAX_CUSTOMER_NAME_CHARS
                    )
            );
        }

        return givenCustomerName;
    }

    /**
     * @return area of at least one hundred, with a scale of 2.
     */
    private BigDecimal promptArea() {
        String area = "";
        while(OrderValidator.isAreaInvalid(area)) {
            area = io.readString("Enter the area sqft in XXX.XX format. It must be exactly to the hundredths place, 100.00 or greater.");
        }
        return GenericValidator.createBigDecimal(area);
    }

    /**
     * Prompts user to enter a valid state abbreviation.
     * Will reprompt until one is matched.
     * @param stateTaxes available
     * @return selected state tax.
     */
    private StateTax promptStateTax(List<StateTax> stateTaxes) {
        StateTax pickedStateTax = null;
        while(pickedStateTax == null) {
            final String givenStateAbbreviation = io.readString("Enter an available state abbreviation.");
            // get the first state abbreviation which exists.
            pickedStateTax = stateTaxes.stream()
                    .filter(stateTax -> stateTax.getStateAbbreviation().equalsIgnoreCase(givenStateAbbreviation))
                    .findFirst()
                    // does not exist so continue the loop
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

    /**
     * Prompts user to enter a valid product type.
     * Will reprompt until one is matched.
     * @param products available
     * @return selected product type.
     */
    private Product promptProduct(List<Product> products) {
        Product pickedProduct = null;
        while(pickedProduct == null) {
            final String givenProductType = io.readString("Enter an available product type.");
            // get the first product which matches the given product type.
            pickedProduct = products.stream()
                    .filter(product -> product.getProductType().equalsIgnoreCase(givenProductType))
                    .findFirst()
                    // does not exist so continue loop
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

    /**
     * Displays the orders found for a date.
     * Will inform the user if the list is empty or null.
     * @param foundOrders to display.
     * @param ordersDate to place in the header.
     */
    public void displayFoundOrders(List<Order> foundOrders, LocalDate ordersDate) {
        if(foundOrders == null || foundOrders.isEmpty()) {
            io.print(
                    String.format(
                            "No orders found for %s.",
                            ordersDate.format(GenericValidator.STR_DATE_FORMATTER)
                    )
            );
        } else {
            io.print(
                    String.format(
                            "Orders found for %s.",
                            ordersDate.format(GenericValidator.STR_DATE_FORMATTER)
                    )
            );
            foundOrders.forEach(System.out::println);
        }
    }

    /**
     * Sets up an order to be placed.
     * If the user does not confirm the placement, an empty optional will be returned.
     * @param products available
     * @param stateTaxes available
     * @return Optional Order
     */
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

        if(io.readBoolean(String.format("Place order?\n%s\n(y/n)", orderToCreate.toString()))) {
            return Optional.of(orderToCreate);
        }
        io.print("Order not placed.");
        return Optional.empty();
    }

    public void displayPressEnterToContinue() {
        io.readString("Press enter to continue...");
    }

    /**
     * Display all available MenuSelection enums.
     * User is not able to choose NONE.
     * @return the selected enum.
     */
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

    /**
     * Copies the order passed in. The copy is edited, and returned.
     * The original passed in order remains.
     * @param orderToEdit will not be changed.
     * @param stateTaxes available.
     * @param products available.
     * @return edited order.
     */
    public Order displayEditOrder(Order orderToEdit, List<StateTax> stateTaxes, List<Product> products) {
        displayHeader(
                String.format(
                        "Editing Order #%d %s",
                        orderToEdit.getOrderNumber(),
                        orderToEdit.getOrderDate().format(GenericValidator.STR_DATE_FORMATTER)
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

    /**
     * Displays an order with removal message.
     * @param orderToRemove will be displayed.
     * @return true if confirmed to remove, false otherwise.
     */
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

    /**
     * Displays the original and edited order for comparison.
     * @param originalOrder will be compared to editedOrder.
     * @param editedOrder will be compared to originalOrder.
     * @return true if edits are confirmed, false otherwise.
     */
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

    public void displayNoOrdersForDate(LocalDate orderDate) {
        displayHeader("No Orders Found");
        io.print(
                String.format(
                        "No orders found on %s.",
                        orderDate.format(GenericValidator.STR_DATE_FORMATTER)
                )
        );
    }

    public void displayOrderNotFound(LocalDate orderDate, int orderNumber) {
        displayHeader("Order Not Found");
        io.print(
                String.format(
                        "No order found for order #%d on %s.",
                        orderNumber,
                        orderDate.format(GenericValidator.STR_DATE_FORMATTER)
                )
        );
    }

    /**
     * Displays an exception with its class name, message, and cause message if it exists.
     * @param e the exception
     */
    public void displayError(Exception e) {
        if(e.getCause() == null) {
            io.print(
                    String.format(
                            "Program could not complete action due to %s.\n\t%s",
                            e.getClass(),
                            e.getMessage()
                    )
            );
        } else {
            io.print(
                    String.format(
                            "Program could not complete action due to %s.\n\t%s\n\t%s",
                            e.getClass(),
                            e.getMessage(),
                            e.getCause().getMessage()
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

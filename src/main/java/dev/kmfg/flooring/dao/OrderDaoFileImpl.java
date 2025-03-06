package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.OrderNotFoundException;
import dev.kmfg.flooring.dto.Order;
import dev.kmfg.flooring.dto.Product;
import dev.kmfg.flooring.dto.StateTax;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderDaoFileImpl implements OrderDao {
    private static final String ORDERS_FILE_HEADER = "OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total";
    private static final String FILE_FORMAT = "/Orders_%02d%02d%02d.txt";
    private static final String FILE_FORMAT_REGEX = "^Orders_\\d{2}\\d{2}\\d{4}\\.txt";
    private static final String DELIMITER = ",";
    private static final int REQUIRED_PARTS = 12;
    private static final DateTimeFormatter INT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMddyyyy");

    private final String ordersPath;
    private final String fileName;

    private HashMap<Integer, Order> orders;

    public OrderDaoFileImpl(String ordersPath) {
        this.ordersPath = ordersPath;
        this.fileName = ordersPath + FILE_FORMAT;
        this.orders = new HashMap<>();
    }

    public OrderDaoFileImpl() {
        this.ordersPath = "Data/Orders";
        this.fileName = ordersPath + FILE_FORMAT;
        this.orders = new HashMap<>();
    }

    private int getNextOrderNumber() {
        return orders.values().stream()
                .mapToInt(Order::getOrderNumber)
                .max()
                .orElse(0) + 1;
    }

    private Order unmarshallOrder(String orderStr, LocalDate orderDate) throws FlooringDataPersistenceException {
        final String[] orderParts = orderStr.split(DELIMITER);
        if(orderParts.length != REQUIRED_PARTS) {
            throw new FlooringDataPersistenceException(
                    String.format(
                            "Malformed data while unmarshalling order. Required %d parts, but received %d.",
                            REQUIRED_PARTS,
                            orderParts.length
                    )
            );
        }

        //  MaterialCost, LaborCost, Tax, Total are ignored as these are calculated values
        try {
            final int orderNumber = Integer.parseInt(orderParts[0]);
            final String customerName = orderParts[1].replace('#', ',');
            final String stateAbbreviation = orderParts[2];
            final BigDecimal taxRate = new BigDecimal(orderParts[3]).setScale(2, RoundingMode.HALF_UP);
            final String productType = orderParts[4];
            final BigDecimal area = new BigDecimal(orderParts[5]).setScale(2, RoundingMode.HALF_UP);
            final BigDecimal costPerSqft = new BigDecimal(orderParts[6]).setScale(2, RoundingMode.HALF_UP);
            final BigDecimal laborCostPerSqft = new BigDecimal(orderParts[7]).setScale(2, RoundingMode.HALF_UP);

            final StateTax stateTax = new StateTax(stateAbbreviation, "Not Loaded", taxRate);
            final Product product = new Product(productType, costPerSqft, laborCostPerSqft);

            return new Order()
                    .setCustomerName(customerName)
                    .setOrderNumber(orderNumber)
                    .setOrderDate(orderDate)
                    .setArea(area)
                    .setStateTax(stateTax)
                    .setProduct(product);
        } catch (NumberFormatException e) {
            throw new FlooringDataPersistenceException(
                    "Malformed data while unmarshalling order.",
                    e
            );
        }
    }

    /**
     * Does not null check any fields on order. May produce NPE if not validated.
     * @param order the order to stringify
     * @return the stringified order
     */
    private String marshallOrder(Order order) {
        return Stream.of(
                        order.getOrderNumber(),
                        order.getCustomerName().replace(',', '#'),
                        order.getStateTax().getStateAbbreviation(),
                        order.getStateTax().getTaxRate(),
                        order.getProduct().getProductType(),
                        order.getArea(),
                        order.getProduct().getCostPerSqft(),
                        order.getProduct().getLaborCostPerSqft(),
                        order.getMaterialCost(),
                        order.getLaborCost(),
                        order.getTax(),
                        order.getTotal()
                )
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    private String getFileName(LocalDate orderDate) {
        return String.format(
                fileName,
                orderDate.getMonthValue(),
                orderDate.getDayOfMonth(),
                orderDate.getYear()
        );
    }

    private void read(LocalDate orderDate) throws FlooringDataPersistenceException {
        Scanner scanner;

        try {
            scanner = new Scanner(
                    new BufferedReader(
                            new FileReader(getFileName(orderDate))
                    )
            );
        } catch (FileNotFoundException e) {
            throw new FlooringDataPersistenceException(
                    "Could not load order data into memory.", e);
        }

        // remove any orders with this current date
        orders.entrySet()
                .removeIf(entry -> entry.getValue().getOrderDate().isEqual(orderDate));

        String currentLine;
        Order order;
        if(scanner.hasNextLine()) {
            scanner.nextLine(); // consume the CSV header.
        }
        while (scanner.hasNextLine()) {
            currentLine = scanner.nextLine();
            order = unmarshallOrder(currentLine, orderDate);
            orders.put(order.getOrderNumber(), order);
        }

        scanner.close();
    }


    private void readAll() throws FlooringDataPersistenceException {
        Path filePath = Paths.get(ordersPath);
        try {
            orders = new HashMap<>();

            for(Path path : Files.list(filePath).collect(Collectors.toList())) {
                // if it's not a file or doesn't match the file format, skip.
                if(!Files.isRegularFile(path) || !path.getFileName().toString().matches(FILE_FORMAT_REGEX)) {
                    continue;
                }
                final String fileName = path.getFileName().toString();
                final String dateStr = fileName.substring(fileName.indexOf('_') + 1, fileName.indexOf('.'));
                read(LocalDate.parse(dateStr, INT_DATE_FORMATTER));
            }
        } catch (IOException e) {
            throw new FlooringDataPersistenceException("Could not read orders path", e);
        }
    }

    private void write(LocalDate orderDate) throws FlooringDataPersistenceException {
        final String fileName = getFileName(orderDate);

        PrintWriter out;

        try {
            final boolean append = false;
            out = new PrintWriter(new FileWriter(fileName, append));

            out.println(ORDERS_FILE_HEADER);
            orders.values().stream()
                    .filter(order -> order.getOrderDate().isEqual(orderDate))
                    .forEach(order -> {
                        out.println(marshallOrder(order));
                        out.flush();
                    });
        } catch (IOException e) {
            throw new FlooringDataPersistenceException(
                    String.format(
                            "Error writing to or open file %s.",
                            fileName
                    ),
                    e
            );
        }

        out.close();
    }

    @Override
    public List<Order> getAllOrders() throws FlooringDataPersistenceException {
        readAll();
        return new ArrayList<>(orders.values());
    }

    @Override
    public List<Order> getAllOrders(LocalDate orderDate) throws FlooringDataPersistenceException {
        read(orderDate);
        return orders.values().stream()
                .filter(order -> order.getOrderDate().isEqual(orderDate))
                .collect(Collectors.toList());
    }

    @Override
    public Order addOrder(Order order) throws FlooringDataPersistenceException {
        readAll();

        order.setOrderNumber(getNextOrderNumber());

        if(orders.containsKey(order.getOrderNumber())) {
            throw new FlooringDataPersistenceException("Cannot add an order that already exists!");
        }

        orders.put(order.getOrderNumber(), order);
        write(order.getOrderDate());
        read(order.getOrderDate());

        return order;
    }

    @Override
    public Order getOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException {
        read(orderDate);

        if(orders.containsKey(orderNumber)) {
            return orders.get(orderNumber);
        } else {
            throw new OrderNotFoundException(
                    String.format(
                            "No order found for %s with id %d",
                            orderDate,
                            orderNumber
                    ),
                    orderDate,
                    orderNumber
            );
        }
    }

    @Override
    public Order editOrder(Order order) throws FlooringDataPersistenceException, OrderNotFoundException {
        read(order.getOrderDate());

        if(!orders.containsKey(order.getOrderNumber())) {
            throw new OrderNotFoundException(
                    "Cannot edit an order that does not exist!",
                    order.getOrderDate(),
                    order.getOrderNumber()
            );
        }

        orders.put(order.getOrderNumber(), order);
        write(order.getOrderDate());

        return order;
    }

    @Override
    public Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException {
        read(orderDate);

        Order removedOrder;
        if(!orders.containsKey(orderNumber) || (removedOrder = orders.remove(orderNumber)) == null) {
            throw new OrderNotFoundException(
                    String.format(
                            "Could not find order #%d to remove.",
                            orderNumber
                    ),
                    orderDate,
                    orderNumber
            );
        }

        write(removedOrder.getOrderDate());

        return removedOrder;
    }
}

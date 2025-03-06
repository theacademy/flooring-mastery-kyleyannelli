package dev.kmfg.flooring.test;

import dev.kmfg.flooring.dao.OrderDao;
import dev.kmfg.flooring.dao.OrderDaoFileImpl;
import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.OrderNotFoundException;
import dev.kmfg.flooring.dto.Order;
import dev.kmfg.flooring.dto.Product;
import dev.kmfg.flooring.dto.StateTax;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class OrderDaoFileImplTest {
    private static final String testOrdersPath = "TestData/Orders";
    private static final String testOrdersBackupPath = "TestData/Restore/Orders";

    private OrderDao testDao;
    private Order testOrder;

    public OrderDaoFileImplTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        // fake products and state taxes are used here because the order dao is not concerned with the
        //  Product and Taxes files.
        //  That will be checked in the service.
        testDao = new OrderDaoFileImpl(testOrdersPath);

        final Product fakeProduct = new Product(
                "Not Real",
                new BigDecimal("2.21").setScale(2, RoundingMode.HALF_UP),
                new BigDecimal("3.23").setScale(2, RoundingMode.HALF_UP)
        );

        final StateTax fakeStateTax = new StateTax(
                "CA",
                "California",
                new BigDecimal("25.00").setScale(2, RoundingMode.HALF_UP)
        );
        // note order number is not set because the dao will generate it
        testOrder = new Order()
                .setOrderDate(LocalDate.now())
                .setCustomerName("John Smith")
                .setArea(new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP))
                .setProduct(fakeProduct)
                .setStateTax(fakeStateTax);
    }

    /**
     * Restores the orders directory to the known good state.
     *
     * @throws IOException if data cannot be restored, we cannot guarantee the tests will run in a known good state.
     */
    @AfterEach
    public void tearDown() throws IOException {
        File modifiedOrdersDirectory = new File(testOrdersPath);

        // require non null was intellij suggestion, I would have done if null continue otherwise
        for(File file : Objects.requireNonNull(modifiedOrdersDirectory.listFiles())) {
            if(!file.isFile()) {
                continue;
            }
            file.delete();
        }

        File backupOrdersDirectory = new File(testOrdersBackupPath);
        for(File file : Objects.requireNonNull(backupOrdersDirectory.listFiles())) {
            Path sourcePath = Paths.get(file.getAbsolutePath());
            Path destPath = Paths.get(modifiedOrdersDirectory.getAbsolutePath(), file.getName());
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    public void testAddOrder() {
        Order addedOrder = null;
        try {
            addedOrder = testDao.addOrder(testOrder);
        } catch(FlooringDataPersistenceException e) {
            fail("Could not add order!", e);
        }

        if(addedOrder == null) {
            fail("Received order from the dao, but it is null");
        }

        // this covers the product and state tax objects as those are checked in equals
        assertEquals(testOrder, addedOrder, "Despite making no changes to the order, the added order does not match the original!");
        final int expectedOrderNumber = 4; // there are already 3 orders
        assertEquals(expectedOrderNumber, addedOrder.getOrderNumber(), "Order number generated does not match expected!");
    }

    @Test
    public void testRemoveExistingOrder() {
        // attempt to get the below
        // 06/01/2013
        // 1,Ada Lovelace,CA,25.00,Tile,249.00,3.50,4.15,871.50,1033.35,476.21,2381.06
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        final LocalDate existingDate = LocalDate.parse("06/01/2013", formatter);
        final int existingId = 1;

        Order existingOrder = null;
        try{
            existingOrder = testDao.removeOrder(existingDate, existingId);
        } catch(FlooringDataPersistenceException e) {
            fail("Could not grab existing order due to data persistence exception.", e);
        } catch(OrderNotFoundException e) {
            fail("Failed to find order that is known to exist!", e);
        }

        if(existingOrder == null) {
            fail("Order was received as null, but did not throw an OrderNotFoundException!");
        }

        // 1,Ada Lovelace,CA,25.00,Tile,249.00,3.50,4.15,871.50,1033.35,476.21,2381.06
        final Order expectedOrder = new Order()
                .setOrderNumber(existingId)
                .setOrderDate(existingDate)
                .setCustomerName("Ada Lovelace")
                .setArea(new BigDecimal("249.00").setScale(2, RoundingMode.HALF_UP))
                .setStateTax(
                        new StateTax(
                                "CA",
                                "Not Loaded", // the order dao cannot load state tax data, this is handled by the service
                                new BigDecimal("25.00").setScale(2, RoundingMode.HALF_UP)
                        )
                )
                // Tile,3.50,4.15
                .setProduct(
                        new Product(
                                "Tile",
                                new BigDecimal("3.50").setScale(2, RoundingMode.HALF_UP),
                                new BigDecimal("4.15").setScale(2, RoundingMode.HALF_UP)
                        )
                );

        assertEquals(existingOrder, expectedOrder, "Existing order that was loaded does not match expected values!");
        assertThrowsExactly(OrderNotFoundException.class, () -> {
            testDao.getOrder(existingDate, existingId);
        }, "Dao failed to throw OrderNotFoundException despite the order being called for removal!");
    }

    @Test
    public void testEditOrderArea() {
        assertDoesNotThrow(() -> testDao.addOrder(testOrder));

        final Order editedOrder = testOrder.cloneOrder();
        editedOrder.setArea(new BigDecimal("5612.23").setScale(2, RoundingMode.HALF_UP));

        assertDoesNotThrow(() -> testDao.editOrder(editedOrder));

        Order editedOrderFromDao = null;
        try {
            editedOrderFromDao = testDao.getOrder(editedOrder.getOrderDate(), editedOrder.getOrderNumber());
        } catch (FlooringDataPersistenceException e) {
            fail("Could not get the order after editing it because of a data persistence error!", e);
        } catch (OrderNotFoundException e) {
            fail("Could not get the order after editing it because the order was not found!", e);
        }

        if(editedOrderFromDao == null) {
            fail("Order was received as null from getOrder, but did not throw OrderNotFoundException");
        }

        assertEquals(editedOrderFromDao.getOrderNumber(), testOrder.getOrderNumber(), "Order number from the dao edited order does not match original!");
        assertEquals(editedOrderFromDao.getCustomerName(), testOrder.getCustomerName(), "Order had customer name changed, but failed to change!");
        assertNotEquals(editedOrderFromDao.getTax(), testOrder.getTax(), "Despite changing the area sqft the tax still matches");
        assertNotEquals(editedOrderFromDao.getTotal(), testOrder.getTotal(), "Despite changing the area sqft the total still matches");
    }

    @Test
    public void testEditOrderCustomerName() {
        assertDoesNotThrow(() -> testDao.addOrder(testOrder));

        final Order editedOrder = testOrder.cloneOrder();
        editedOrder.setCustomerName("James Lemon");

        assertDoesNotThrow(() -> testDao.editOrder(editedOrder));

        Order editedOrderFromDao = null;
        try {
            editedOrderFromDao = testDao.getOrder(editedOrder.getOrderDate(), editedOrder.getOrderNumber());
        } catch (FlooringDataPersistenceException e) {
            fail("Could not get the order after editing it because of a data persistence error!", e);
        } catch (OrderNotFoundException e) {
            fail("Could not get the order after editing it because the order was not found!", e);
        }

        if(editedOrderFromDao == null) {
            fail("Order was received as null from getOrder, but did not throw OrderNotFoundException");
        }

        assertEquals(editedOrderFromDao.getOrderNumber(), testOrder.getOrderNumber(), "Order number from the dao edited order does not match original!");
        assertNotEquals(editedOrderFromDao.getCustomerName(), testOrder.getCustomerName(), "Order had customer name changed, but failed to change!");
        assertEquals(editedOrderFromDao.getTax(), testOrder.getTax(), "Despite not changing product, state, or area, the tax no longer matches");
        assertEquals(editedOrderFromDao.getTotal(), testOrder.getTotal(), "Despite not changing product, state, or area, the totals no longer match");
    }

    @Test
    public void testGetAllOrdersFromFile() {
        final List<Order> expectedOrders = new ArrayList<>();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        final LocalDate existingDateOne = LocalDate.parse("06/01/2013", formatter);
        final LocalDate existingDateTwo = LocalDate.parse("06/02/2013", formatter);
        // 1,Ada Lovelace,CA,25.00,Tile,249.00,3.50,4.15,871.50,1033.35,476.21,2381.06
        final Order orderOne = new Order()
                .setOrderNumber(1)
                .setOrderDate(existingDateOne)
                .setCustomerName("Ada Lovelace")
                .setArea(new BigDecimal("249.00").setScale(2, RoundingMode.HALF_UP))
                .setStateTax(
                        new StateTax(
                                "CA",
                                "Not Loaded", // the order dao cannot load state tax data, this is handled by the service
                                new BigDecimal("25.00").setScale(2, RoundingMode.HALF_UP)
                        )
                )
                // Tile,3.50,4.15
                .setProduct(
                        new Product(
                                "Tile",
                                new BigDecimal("3.50").setScale(2, RoundingMode.HALF_UP),
                                new BigDecimal("4.15").setScale(2, RoundingMode.HALF_UP)
                        )
                );
        expectedOrders.add(orderOne);
        // 2,Doctor Who,WA,9.25,Wood,243.00,5.15,4.75,1251.45,1154.25,216.51,2622.21
        final Order orderTwo = new Order()
                .setOrderNumber(2)
                .setOrderDate(existingDateTwo)
                .setCustomerName("Doctor Who")
                .setArea(new BigDecimal("243.00").setScale(2, RoundingMode.HALF_UP))
                .setStateTax(
                        new StateTax(
                                "WA",
                                "Not Loaded", // the order dao cannot load state tax data, this is handled by the service
                                new BigDecimal("9.25").setScale(2, RoundingMode.HALF_UP)
                        )
                )
                // Wood,5.15,4.75
                .setProduct(
                        new Product(
                                "Wood",
                                new BigDecimal("5.15").setScale(2, RoundingMode.HALF_UP),
                                new BigDecimal("4.75").setScale(2, RoundingMode.HALF_UP)
                        )
                );
        expectedOrders.add(orderTwo);
        // 3,Albert Einstein,KY,6.00,Carpet,217.00,2.25,2.10,488.25,455.70,56.64,1000.59
        final Order orderThree = new Order()
                .setOrderNumber(3)
                .setOrderDate(existingDateTwo)
                .setCustomerName("Albert Einstein")
                .setArea(new BigDecimal("217.00").setScale(2, RoundingMode.HALF_UP))
                .setStateTax(
                        new StateTax(
                                "KY",
                                "Not Loaded", // the order dao cannot load state tax data, this is handled by the service
                                new BigDecimal("6.00").setScale(2, RoundingMode.HALF_UP)
                        )
                )
                // Carpet,2.25,2.10
                .setProduct(
                        new Product(
                                "Carpet",
                                new BigDecimal("2.25").setScale(2, RoundingMode.HALF_UP),
                                new BigDecimal("2.10").setScale(2, RoundingMode.HALF_UP)
                        )
                );
        expectedOrders.add(orderThree);

        List<Order> orders = null;
        try {
            orders = testDao.getAllOrders();
        } catch(FlooringDataPersistenceException e) {
            fail("Could not fetch orders!", e);
        }

        if(orders == null) {
            fail("Orders received as null list!");
        }

        assertEquals(orders.size(), expectedOrders.size(), "Unexpected number of orders!");

        orders.sort(Comparator.comparing(Order::getOrderNumber));
        expectedOrders.sort(Comparator.comparing(Order::getOrderNumber));

        for(int i = 0; i < orders.size(); i++) {
            assertEquals(orders.get(i), expectedOrders.get(i), "Orders are not equal!");
        }
    }
}

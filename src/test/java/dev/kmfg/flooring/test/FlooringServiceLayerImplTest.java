package dev.kmfg.flooring.test;

import dev.kmfg.flooring.dao.*;
import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.OrderNotFoundException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.model.Order;
import dev.kmfg.flooring.model.Product;
import dev.kmfg.flooring.model.StateTax;
import dev.kmfg.flooring.service.FlooringServiceLayer;
import dev.kmfg.flooring.service.FlooringServiceLayerImpl;
import dev.kmfg.flooring.service.exception.OrderDataValidationException;
import dev.kmfg.flooring.service.validator.GenericValidator;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class FlooringServiceLayerImplTest {
    private static final String testOrdersPath = "TestData/Orders";
    private static final String testOrdersBackupPath = "TestData/Restore/Orders";
    private static final String testProductsFilename = "TestData/Products.txt";
    private static final String testTaxesFilename = "TestData/Taxes.txt";

    private FlooringServiceLayer service;
    private Order testOrder;

    public FlooringServiceLayerImplTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        final OrderDao orderDao = new OrderDaoFileImpl(testOrdersPath);
        final ProductDao productDao = new ProductDaoFileImpl(testProductsFilename);
        final StateTaxDao stateTaxDao = new StateTaxDaoFileImpl(testTaxesFilename);
        service = new FlooringServiceLayerImpl(orderDao, productDao, stateTaxDao);

        // unlike the daos, the service should care about the orders underlying fields.
        //  data validation should fail if unknown products or state taxes are used.
        final Product knownProduct = new Product(
                "Tile",
                GenericValidator.createBigDecimal("3.50"),
                GenericValidator.createBigDecimal("4.15")
        );

        final StateTax knownStateTax = new StateTax(
                "CA",
                "California",
                GenericValidator.createBigDecimal("25.00")
        );
        // note order number is not set because the dao will generate it
        testOrder = new Order()
                .setOrderDate(LocalDate.now())
                .setCustomerName("John Smith")
                .setArea(GenericValidator.createBigDecimal("100.00"))
                .setProduct(knownProduct)
                .setStateTax(knownStateTax);
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
            assertTrue(file.delete(), "Test files could not be deleted. Subsequent tests are no longer in known good state!");
        }

        File backupOrdersDirectory = new File(testOrdersBackupPath);
        for(File file : Objects.requireNonNull(backupOrdersDirectory.listFiles())) {
            Path sourcePath = Paths.get(file.getAbsolutePath());
            Path destPath = Paths.get(modifiedOrdersDirectory.getAbsolutePath(), file.getName());
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Our OrderDao cannot get information from StateTaxDao, thus,
     *  the service should be populating the order with this information.
     */
    @Test
    public void testServiceGetMissingOrderData() {
        List<Order> orders = null;
        try {
            orders = service.getAllOrders();
        } catch(FlooringDataPersistenceException e) {
            fail("Failed to get all orders due to persistence exception!", e);
        } catch(StateTaxNotFoundException e) {
            fail("Failed to collect state taxes for orders!", e);
        } catch(OrderNotFoundException e) {
            fail("No valid order directories were found!", e);
        }

        assertEquals(orders.size(), 3, "Orders received does not match expected size!");

        orders.forEach(order -> {
            assertNotEquals(order.getStateTax(), null, "Order has null StateTax!");
            assertNotEquals(order.getStateTax().getStateName(), "Not Found", "Order did not have state name populated!");
        });

    }

    @Test
    public void testOrderValidation() {
        // try to add an order with an area less than allowed
        final BigDecimal originalArea = testOrder.getArea();
        testOrder.setArea(GenericValidator.createBigDecimal("99.99"));
        assertThrowsExactly(OrderDataValidationException.class, () -> {
            service.addOrder(testOrder);
        });
        // restore the area
        testOrder.setArea(originalArea);

        // now it should validate that everything is restored.
        assertDoesNotThrow(() -> {
            service.addOrder(testOrder);
            service.removeOrder(testOrder.getOrderDate(), testOrder.getOrderNumber());
        });

        // try to add an order with a null product
        final Product originalProduct = testOrder.getProduct();
        testOrder.setProduct(null);
        assertThrowsExactly(OrderDataValidationException.class, () -> {
            service.addOrder(testOrder);
        });
        // restore the product
        testOrder.setProduct(originalProduct);

        // now it should validate that everything is restored.
        assertDoesNotThrow(() -> {
            service.addOrder(testOrder);
            service.removeOrder(testOrder.getOrderDate(), testOrder.getOrderNumber());
        });

        // attempt to add a fake product
        final Product originalKnownProduct = testOrder.getProduct();
        testOrder.setProduct(new Product("NR", GenericValidator.createBigDecimal("0"), GenericValidator.createBigDecimal("1")));
        assertThrowsExactly(OrderDataValidationException.class, () -> {
            service.addOrder(testOrder);
        });
        // restore the known product
        testOrder.setProduct(originalKnownProduct);

        // now it should validate that everything is restored.
        assertDoesNotThrow(() -> {
            service.addOrder(testOrder);
            service.removeOrder(testOrder.getOrderDate(), testOrder.getOrderNumber());
        });

        final StateTax originalKnownStateTax = testOrder.getStateTax();
        testOrder.setStateTax(new StateTax("NR", "Not Real", GenericValidator.createBigDecimal("1.1")));
        assertThrowsExactly(OrderDataValidationException.class, () -> {
            service.addOrder(testOrder);
        });
        // restore the known state tax
        testOrder.setStateTax(originalKnownStateTax);

        assertDoesNotThrow(() -> service.addOrder(testOrder));
    }
}

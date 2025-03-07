package dev.kmfg.flooring.test;

import dev.kmfg.flooring.dao.ProductDao;
import dev.kmfg.flooring.dao.ProductDaoFileImpl;
import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.ProductNotFoundException;
import dev.kmfg.flooring.model.Product;
import dev.kmfg.flooring.service.validator.GenericValidator;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDaoFileImplTest {
    private static final String testProductsFile = "TestData/Products.txt";

    private ProductDao testDao;

    public ProductDaoFileImplTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        testDao = new ProductDaoFileImpl(testProductsFile);
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testGetAllProducts() {
        List<Product> products = null;

        try {
             products = testDao.getAllProducts();
        } catch(FlooringDataPersistenceException e) {
            fail("There was an issue getting all the products!", e);
        }

        if(products == null) {
            fail("The products were received, but a null list was given.");
        }

        assertEquals(products.size(), 4, "The expected number of products was not met.");
    }

    @Test
    public void testGetUnknownProductType() {
        final String unknownProductType = "Not Real";
        assertThrowsExactly(ProductNotFoundException.class, () -> testDao.getProduct(unknownProductType));
    }

    @Test
    public void testGetKnownProductType() {
        final String knownProductType = "Tile";

        Product product = null;
        try {
            product = testDao.getProduct(knownProductType);
        } catch(ProductNotFoundException e) {
            fail("The known product type could not be found!", e);
        } catch(FlooringDataPersistenceException e) {
            fail("There was an issue getting the known product!", e);
        }

        if(product == null) {
            fail("The known product was received, but is null");
        }

        final BigDecimal knownCostPerSqft = GenericValidator.createBigDecimal("3.50");
        final BigDecimal knownLaborCostPerSqft = GenericValidator.createBigDecimal("4.15");

        assertTrue(knownProductType.equalsIgnoreCase(product.getProductType()), "The product type does not match the expected name!");
        assertEquals(knownCostPerSqft, product.getCostPerSqft(), "The cost per sqft does not match the expected value!");
        assertEquals(knownLaborCostPerSqft, product.getLaborCostPerSqft(), "The labor cost per sqft does not match the expected value!");
    }
}

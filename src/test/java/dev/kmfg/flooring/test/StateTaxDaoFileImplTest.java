package dev.kmfg.flooring.test;

import dev.kmfg.flooring.dao.StateTaxDao;
import dev.kmfg.flooring.dao.StateTaxDaoFileImpl;
import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.dto.Product;
import dev.kmfg.flooring.dto.StateTax;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StateTaxDaoFileImplTest {
    private static final String testStateTaxesFile = "TestData/Taxes.txt";

    private StateTaxDao testDao;

    public StateTaxDaoFileImplTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() throws Exception {
        testDao = new StateTaxDaoFileImpl(testStateTaxesFile);
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testGetAllProducts() {
        List<StateTax> stateTaxes = null;

        try {
            stateTaxes = testDao.getAllStateTaxes();
        } catch(FlooringDataPersistenceException e) {
            fail("There was an issue getting the state taxes!", e);
        }

        if(stateTaxes == null) {
            fail("The state taxes were received, but a null list was given.");
        }

        assertEquals(stateTaxes.size(), 4, "The expected number of state taxes was not met.");
    }

    @Test
    public void testGetKnownStateAbbreviation() {
        final String knownStateAbbreviation = "KY";

        StateTax stateTax = null;
        try {
            stateTax = testDao.getStateTax(knownStateAbbreviation);
        } catch(StateTaxNotFoundException e) {
            fail("The known state tax could not be found!", e);
        } catch(FlooringDataPersistenceException e) {
            fail("There was an issue getting the known state tax!", e);
        }

        if(stateTax == null) {
            fail("The known state tax was received, but is null");
        }

        final String knownStateName = "Kentucky";
        final BigDecimal knownTaxRate = new BigDecimal("6.00").setScale(2, RoundingMode.HALF_UP);

        assertTrue(knownStateName.equalsIgnoreCase(stateTax.getStateName()), "The state name does not match the expected name!");
        assertTrue(knownStateAbbreviation.equalsIgnoreCase(stateTax.getStateAbbreviation()), "The state abbreviation does not match the expected abbreviation!");
        assertEquals(knownTaxRate, stateTax.getTaxRate(), "The tax rate does not match the expected value!");
    }
}

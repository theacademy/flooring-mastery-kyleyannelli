package dev.kmfg.flooring.test;

import dev.kmfg.flooring.dao.StateTaxDao;
import dev.kmfg.flooring.dao.StateTaxDaoFileImpl;
import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.model.StateTax;
import dev.kmfg.flooring.service.validator.GenericValidator;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
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
    public void setUp() {
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
    public void testGetUnknownStateAbbreviation() {
        final String knownStateAbbreviation = "NR";
        assertThrowsExactly(StateTaxNotFoundException.class, () -> testDao.getStateTax(knownStateAbbreviation));
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
        final BigDecimal knownTaxRate = GenericValidator.createBigDecimal("6.00");

        assertTrue(knownStateName.equalsIgnoreCase(stateTax.getStateName()), "The state name does not match the expected name!");
        assertTrue(knownStateAbbreviation.equalsIgnoreCase(stateTax.getStateAbbreviation()), "The state abbreviation does not match the expected abbreviation!");
        assertEquals(knownTaxRate, stateTax.getTaxRate(), "The tax rate does not match the expected value!");
    }
}

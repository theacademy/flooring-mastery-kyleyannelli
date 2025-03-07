package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.model.StateTax;
import dev.kmfg.flooring.service.validator.GenericValidator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class StateTaxDaoFileImpl implements StateTaxDao {
    private static final String DELIMITER = ",";
    private static final int REQUIRED_PARTS = 3;

    private String fileName = "Data/Taxes.txt";
    private HashMap<String, StateTax> stateTaxes;

    public StateTaxDaoFileImpl(String fileName) {
        this.fileName = fileName;
        this.stateTaxes = new HashMap<>();
    }

    public StateTaxDaoFileImpl() {
        this.stateTaxes = new HashMap<>();
    }

    private StateTax unmarshallStateTax(String productStr) throws FlooringDataPersistenceException {
        final String[] productParts = productStr.split(DELIMITER);
        if(productParts.length != REQUIRED_PARTS) {
            throw new FlooringDataPersistenceException(
                    String.format(
                            "Malformed data while unmarshalling product. Required %d parts, but received %d.",
                            REQUIRED_PARTS,
                            productParts.length
                    )
            );
        }

        final String stateAbbreviation = productParts[0];
        final String stateName = productParts[1];
        final BigDecimal taxRate = GenericValidator.createBigDecimal(productParts[2]);

        return new StateTax(stateAbbreviation, stateName, taxRate);
    }

    private void read() throws FlooringDataPersistenceException {
        Scanner scanner;

        try {
            scanner = new Scanner(
                    new BufferedReader(
                            new FileReader(fileName)));
        } catch (FileNotFoundException e) {
            throw new FlooringDataPersistenceException(
                    "Could not load state tax data into memory.", e);
        }

        stateTaxes = new HashMap<>();

        String currentLine;
        StateTax stateTax;
        if(scanner.hasNextLine()) {
            scanner.nextLine(); // consume the CSV header.
        }
        while (scanner.hasNextLine()) {
            currentLine = scanner.nextLine();
            stateTax = unmarshallStateTax(currentLine);
            stateTaxes.put(stateTax.getStateAbbreviation(), stateTax);
        }

        scanner.close();
    }


    @Override
    public StateTax getStateTax(String stateAbbreviation) throws FlooringDataPersistenceException, StateTaxNotFoundException {
        read();

        if(stateTaxes.containsKey(stateAbbreviation)) {
            return stateTaxes.get(stateAbbreviation);
        } else {
            throw new StateTaxNotFoundException(String.format("No state was found for the abbreviation %s", stateAbbreviation));
        }
    }

    @Override
    public List<StateTax> getAllStateTaxes() throws FlooringDataPersistenceException {
        read();

        return new ArrayList<>(stateTaxes.values());
    }
}

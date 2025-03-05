package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.dto.StateTax;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StateTaxDaoFileImpl implements StateTaxDao {
    private static final String FILE_NAME = "Data/Taxes.txt";
    private static final String DELIMITER = ",";
    private static final int REQUIRED_PARTS = 3;

    private HashMap<String, StateTax> stateTaxes;

    public StateTaxDaoFileImpl() {
        this.stateTaxes = new HashMap<String, StateTax>();
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
        final BigDecimal taxRate = new BigDecimal(productParts[2]).setScale(2, RoundingMode.HALF_UP);

        return new StateTax(stateAbbreviation, stateName, taxRate);
    }

    private String marshallStateTax(StateTax stateTax) throws FlooringDataPersistenceException {
        if(stateTax == null) {
            throw new FlooringDataPersistenceException("Cannot marshall a null state tax!");
        } else if(stateTax.getStateAbbreviation() == null ||
                stateTax.getStateName() == null ||
                stateTax.getTaxRate() == null) {
            throw new FlooringDataPersistenceException("Cannot marshall a state tax with null parts!");
        }

        return Stream.of(
                stateTax.getStateAbbreviation(),
                stateTax.getStateName(),
                stateTax.getTaxRate().toString()
        ).collect(Collectors.joining(DELIMITER));
    }

    private void read() throws FlooringDataPersistenceException {
        Scanner scanner;

        try {
            scanner = new Scanner(
                    new BufferedReader(
                            new FileReader(FILE_NAME)));
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

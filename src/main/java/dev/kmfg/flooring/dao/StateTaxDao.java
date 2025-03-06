package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.dto.StateTax;

import java.util.List;

public interface StateTaxDao {
    /**
     * Gets the state by state abbreviation.
     *  For example, NY would fetch the New York State Tax, if it exists.
     * @param stateAbbreviation such as NY
     * @return the StateTax, if it exists.
     * @throws FlooringDataPersistenceException if there was an issue fetching the StateTax.
     * @throws StateTaxNotFoundException if the StateTax does not exist.
     */
    StateTax getStateTax(String stateAbbreviation) throws FlooringDataPersistenceException, StateTaxNotFoundException;

    /**
     * Gets all the StateTaxes.
     * @return all StateTaxes
     * @throws FlooringDataPersistenceException if there was an issue fetching the StateTaxes.
     */
    List<StateTax> getAllStateTaxes() throws FlooringDataPersistenceException;
}

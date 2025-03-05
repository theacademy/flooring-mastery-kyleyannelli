package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.dto.StateTax;

import java.util.List;

public interface StateTaxDao {
    StateTax getStateTax(String stateAbbreviation) throws FlooringDataPersistenceException, StateTaxNotFoundException;
    List<StateTax> getAllStateTaxes() throws FlooringDataPersistenceException;
}

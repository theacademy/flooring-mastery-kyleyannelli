package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.ProductNotFoundException;
import dev.kmfg.flooring.dto.Product;

import java.util.List;

public interface ProductDao {
    Product getProduct(String productType) throws FlooringDataPersistenceException, ProductNotFoundException;
    List<Product> getAllProducts() throws FlooringDataPersistenceException;
}

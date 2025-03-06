package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.ProductNotFoundException;
import dev.kmfg.flooring.dto.Product;

import java.util.List;

public interface ProductDao {
    /**
     * Gets the product by product type.
     * @param productType for the product
     * @return the product, if it exists.
     * @throws FlooringDataPersistenceException if there was an issue fetching the product.
     * @throws ProductNotFoundException if the product does not exist via the given product type.
     */
    Product getProduct(String productType) throws FlooringDataPersistenceException, ProductNotFoundException;

    /**
     * Gets all products.
     * @return every product
     * @throws FlooringDataPersistenceException if the products cannot be fetched.
     */
    List<Product> getAllProducts() throws FlooringDataPersistenceException;
}

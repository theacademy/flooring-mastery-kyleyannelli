package dev.kmfg.flooring.service;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.OrderNotFoundException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.dto.Order;
import dev.kmfg.flooring.dto.Product;
import dev.kmfg.flooring.dto.StateTax;
import dev.kmfg.flooring.service.exception.OrderDataValidationException;

import java.time.LocalDate;
import java.util.List;

public interface FlooringServiceLayer {
    /**
     * Gets all products.
     * @return every product
     * @throws FlooringDataPersistenceException if the products cannot be fetched.
     */
    List<Product> getAllProducts() throws FlooringDataPersistenceException;

    /**
     * Gets all the StateTaxes.
     * @return all StateTaxes
     * @throws FlooringDataPersistenceException if there was an issue fetching the StateTaxes.
     */
    List<StateTax> getAllStateTaxes() throws FlooringDataPersistenceException;

    /**
     * Gets all orders and dates.
     * Orders will have fully populated StateTaxes.
     * @return all orders
     * @throws FlooringDataPersistenceException if the orders cannot be fetched.
     */
    List<Order> getAllOrders() throws FlooringDataPersistenceException, StateTaxNotFoundException;

    /**
     * Gets all orders for specified date.
     * Orders will have fully populated StateTaxes.
     * @param orderDate to look for orders
     * @return all orders for the specified date.
     * @throws FlooringDataPersistenceException if the orders cannot be fetched.
     */
    List<Order> getAllOrders(LocalDate orderDate) throws FlooringDataPersistenceException;

    /**
     * Adds an order, if it does not already exist.
     * @param order the order to add
     * @return the added order
     * @throws FlooringDataPersistenceException if there is an issue persisting the order.
     * @throws OrderDataValidationException if the order data is invalid.
     */
    Order addOrder(Order order) throws FlooringDataPersistenceException, OrderDataValidationException;

    /**
     * Gets an order, with its fully populated StateTax.
     * @param orderDate for the order
     * @param orderNumber for the order
     * @return order fully populated, if it exists.
     * @throws FlooringDataPersistenceException if there is an issue fetching the order.
     * @throws OrderNotFoundException if it does not exist.
     * @throws StateTaxNotFoundException if it has a state tax that does not exist.
     */
    Order getOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException, StateTaxNotFoundException;

    /**
     * Edits an order, if it already exists.
     * @param order the order to edit
     * @return the edited order
     * @throws FlooringDataPersistenceException if there is an issue persisting the order.
     * @throws OrderNotFoundException if it doesn't exist.
     * @throws OrderDataValidationException if the order data is invalid.
     */
    Order editOrder(Order order) throws FlooringDataPersistenceException, OrderNotFoundException, OrderDataValidationException;

    /**
     * Removes an order.
     * @param orderDate for the order
     * @param orderNumber for the order
     * @return the removed order
     * @throws FlooringDataPersistenceException if there is an issue persisting the order.
     * @throws OrderNotFoundException if it doesn't exist.
     */
    Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException;

    /**
     * Ensures the given order has all the required fields to allow for data persistence.
     * @param order to validate
     * @return the validated order
     * @throws OrderDataValidationException if the order or any of its fields have invalid data
     */
    Order validateEntireOrder(Order order) throws OrderDataValidationException;
}

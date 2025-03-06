package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.OrderNotFoundException;
import dev.kmfg.flooring.dto.Order;

import java.time.LocalDate;
import java.util.List;

public interface OrderDao {
    /**
     * Gets all orders and dates.
     * Orders will be MISSING their StateTax State Name because this class cannot
     *  query the StateTaxDao
     * @return all orders
     * @throws FlooringDataPersistenceException if the orders cannot be fetched.
     */
    List<Order> getAllOrders() throws FlooringDataPersistenceException, OrderNotFoundException;

    /**
     * Gets all orders for specific date.
     * Orders will be MISSING their StateTax State Name because this class cannot
     *  query the StateTaxDao
     * @param orderDate the date to get orders for
     * @return all orders for specific data
     * @throws FlooringDataPersistenceException if the orders cannot be fetched.
     */
    List<Order> getAllOrders(LocalDate orderDate) throws FlooringDataPersistenceException, OrderNotFoundException;

    /**
     * Adds an order, if it does not already exist.
     * @param order the order to add
     * @return the added order
     * @throws FlooringDataPersistenceException if there is an issue persisting the order.
     * @throws OrderNotFoundException if the order failed to collect after saving.
     */
    Order addOrder(Order order) throws FlooringDataPersistenceException, OrderNotFoundException;

    /**
     * Gets an order.
     * @param orderDate the date for the order
     * @param orderNumber the order number (id)
     * @return the order, if it exists.
     * @throws FlooringDataPersistenceException if there is an issue fetching the order.
     * @throws OrderNotFoundException if the order does not exist for the orderDate and orderNumber.
     */
    Order getOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException;

    /**
     * The order to edit. It MUST already exist.
     * @param order to overwrite.
     * @return the order, if it exists.
     * @throws FlooringDataPersistenceException if there is an issue fetching the order.
     * @throws OrderNotFoundException if the order does not exist for the orderDate and orderNumber.
     */
    Order editOrder(Order order) throws FlooringDataPersistenceException, OrderNotFoundException;

    /**
     * The order to remove.
     * @param orderDate the date for the order
     * @param orderNumber the order number (id)
     * @return the order, if it exists.
     * @throws FlooringDataPersistenceException if there is an issue fetching or removing the order.
     * @throws OrderNotFoundException if the order does not exist for the orderDate and orderNumber.
     */
    Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException;
}

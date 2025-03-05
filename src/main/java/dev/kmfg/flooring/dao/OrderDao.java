package dev.kmfg.flooring.dao;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.OrderNotFoundException;
import dev.kmfg.flooring.dto.Order;

import java.time.LocalDate;
import java.util.List;

public interface OrderDao {
    List<Order> getAllOrders() throws FlooringDataPersistenceException;
    List<Order> getAllOrders(LocalDate orderDate) throws FlooringDataPersistenceException;
    Order addOrder(Order order) throws FlooringDataPersistenceException;
    Order getOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException;
    Order editOrder(Order order) throws FlooringDataPersistenceException, OrderNotFoundException;
    Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException;
}

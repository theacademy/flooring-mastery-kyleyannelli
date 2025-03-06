package dev.kmfg.flooring.service;

import dev.kmfg.flooring.dao.exception.FlooringDataPersistenceException;
import dev.kmfg.flooring.dao.exception.OrderNotFoundException;
import dev.kmfg.flooring.dao.exception.ProductNotFoundException;
import dev.kmfg.flooring.dao.exception.StateTaxNotFoundException;
import dev.kmfg.flooring.dto.Order;
import dev.kmfg.flooring.dto.Product;
import dev.kmfg.flooring.dto.StateTax;
import dev.kmfg.flooring.service.exception.OrderDataValidationException;

import java.time.LocalDate;
import java.util.List;

public interface FlooringServiceLayer {
    public Product getProduct(String productType) throws FlooringDataPersistenceException, ProductNotFoundException;
    public List<Product> getAllProducts() throws FlooringDataPersistenceException;
    public StateTax getStateTax(String stateAbbreviation) throws FlooringDataPersistenceException, StateTaxNotFoundException;
    public List<StateTax> getAllStateTaxes() throws FlooringDataPersistenceException;
    public List<Order> getAllOrders() throws FlooringDataPersistenceException, StateTaxNotFoundException;
    public List<Order> getAllOrders(LocalDate orderDate) throws FlooringDataPersistenceException;
    public Order addOrder(Order order) throws FlooringDataPersistenceException, OrderDataValidationException;
    public Order getOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException, StateTaxNotFoundException;
    public Order editOrder(Order order) throws FlooringDataPersistenceException, OrderNotFoundException, OrderDataValidationException;
    public Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringDataPersistenceException, OrderNotFoundException;
    public int getTotalOrderCount() throws FlooringDataPersistenceException;
}

package dev.kmfg.flooring.dao.exception;

import java.time.LocalDate;

public class OrderNotFoundException extends Exception {
    private final LocalDate orderDate;
    private final int orderNumber;
    private final boolean thereAreAnyOrdersInDate;

    public OrderNotFoundException(String msg, LocalDate orderDate, int orderNumber) {
        super(msg);
        this.orderDate = orderDate;
        this.orderNumber = orderNumber;
        this.thereAreAnyOrdersInDate = orderNumber == 1;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public boolean areThereAnyOrdersInDate() {
        return thereAreAnyOrdersInDate;
    }
}

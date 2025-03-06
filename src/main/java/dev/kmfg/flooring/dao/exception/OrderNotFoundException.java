package dev.kmfg.flooring.dao.exception;

import java.time.LocalDate;

public class OrderNotFoundException extends Exception {
    private final LocalDate orderDate;
    private final int orderNumber;

    public OrderNotFoundException(String msg, LocalDate orderDate, int orderNumber) {
        super(msg);
        this.orderDate = orderDate;
        this.orderNumber = orderNumber;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public int getOrderNumber() {
        return orderNumber;
    }
}

package dev.kmfg.flooring.service.exception;

public class OrderDataValidationException extends Exception {
    public OrderDataValidationException(String msg) {
        super(msg);
    }

    public OrderDataValidationException(String msg, Throwable t) {
        super(msg, t);
    }
}

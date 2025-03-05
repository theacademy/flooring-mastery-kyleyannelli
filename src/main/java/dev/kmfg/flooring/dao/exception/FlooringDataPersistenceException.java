package dev.kmfg.flooring.dao.exception;

public class FlooringDataPersistenceException extends Exception {
    public FlooringDataPersistenceException(String msg) {
        super(msg);
    }

    public FlooringDataPersistenceException(String msg, Throwable t) {
        super(msg, t);
    }
}

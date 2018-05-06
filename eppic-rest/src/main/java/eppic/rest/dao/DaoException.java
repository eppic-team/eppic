package eppic.rest.dao;

public class DaoException extends Exception {

    public DaoException() {

    }

    public DaoException(Throwable e) {
        super(e);
    }

    public DaoException(String message) {
        super(message);
    }
}

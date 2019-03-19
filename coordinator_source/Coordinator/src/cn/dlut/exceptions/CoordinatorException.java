package cn.dlut.exceptions;

public class CoordinatorException extends Exception {

    private static final long serialVersionUID = 1L;

    public CoordinatorException(final Exception e) {
        super(e);
    }

}

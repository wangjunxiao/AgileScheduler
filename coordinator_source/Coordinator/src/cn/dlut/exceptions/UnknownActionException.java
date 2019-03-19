package cn.dlut.exceptions;

public class UnknownActionException extends Exception {

    private static final long serialVersionUID = 1L;

    public UnknownActionException(final String msg) {
        super(msg);
    }

}

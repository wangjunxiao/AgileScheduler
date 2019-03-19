package cn.dlut.exceptions;

public class SwitchStateException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public SwitchStateException() {
        super();
    }

    public SwitchStateException(final String msg) {
        super(msg);
    }

    public SwitchStateException(final Throwable msg) {
        super(msg);
    }

}

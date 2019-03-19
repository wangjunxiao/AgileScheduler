package cn.dlut.exceptions;

public class ControllerStateException extends IllegalArgumentException {

    private static final long serialVersionUID = 6957434977838246116L;

    public ControllerStateException() {
        super();
    }

    public ControllerStateException(final String msg) {
        super(msg);
    }

    public ControllerStateException(final Throwable msg) {
        super(msg);
    }
}

package cn.dlut.exceptions;

public class MissingRequiredField extends Exception {

    private static final long serialVersionUID = 1L;

    public MissingRequiredField(final String fieldName) {
        super(fieldName + " is required ");
    }

}

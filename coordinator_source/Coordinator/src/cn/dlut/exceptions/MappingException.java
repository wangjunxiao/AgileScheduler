package cn.dlut.exceptions;

/**
 * A exception for errors thrown when a mapping cannot find an element i.e. a
 * provided key is not mapped to any value. Intended to be a superclass for a
 * class of Mappable-related fetch failures that return null.
 */
@SuppressWarnings("rawtypes")
public class MappingException extends Exception {
    /* OVX */
    private static final long serialVersionUID = 798688L;

    MappingException() {
        super();
    }

    MappingException(String cause) {
        super(cause);
    }

    public MappingException(Object key, Class value) {
        super(value.getName() + " not found for key: \n\t"
                + key.getClass().getName() + ":\n\t[" + key.toString() + "]");
    }

    MappingException(Throwable cause) {
        super(cause);
    }

}

package com.icecreamy.util;

public class IceCreamyException extends Exception {
    private static final long serialVersionUID = -5646946067952474915L;

    public IceCreamyException(String message) {
        super(message);
    }

    public IceCreamyException(String message, int entityId) {
        super(String.format(message + ", Entity ID: %d", entityId));
    }

    public IceCreamyException(String message, Throwable throwable) {
        super(message, throwable);
    }

}

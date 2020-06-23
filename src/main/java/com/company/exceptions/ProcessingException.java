package com.company.exceptions;

public final class ProcessingException extends IllegalStateException {

    public ProcessingException(final String s) {
        super(s);
    }

    public ProcessingException(final Throwable cause) {
        super(cause);
    }
}

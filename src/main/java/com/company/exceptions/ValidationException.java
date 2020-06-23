package com.company.exceptions;

public final class ValidationException extends IllegalArgumentException {

    public ValidationException(final String s) {
        super(s);
    }
}

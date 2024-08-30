package org.example.debeziumapp2.exception;

public class ChangeNotFoundException extends RuntimeException {

    public ChangeNotFoundException(String message) {
        super(message);
    }
}

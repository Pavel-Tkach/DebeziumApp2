package org.example.debeziumapp2.exception;

public class ChangeExecutedException extends RuntimeException {
    public ChangeExecutedException(String message) {
        super(message);
    }
}

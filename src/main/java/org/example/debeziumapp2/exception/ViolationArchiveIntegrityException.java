package org.example.debeziumapp2.exception;

public class ViolationArchiveIntegrityException extends RuntimeException {

    public ViolationArchiveIntegrityException(String message) {
        super(message);
    }
}

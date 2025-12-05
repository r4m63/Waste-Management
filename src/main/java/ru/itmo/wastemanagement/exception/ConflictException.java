package ru.itmo.wastemanagement.exception;

public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
}


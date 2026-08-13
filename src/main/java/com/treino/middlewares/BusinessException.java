package com.treino.middlewares;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

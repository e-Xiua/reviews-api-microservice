package com.iwellness.reviews.exception;

public class UnauthorizedReviewAccessException extends RuntimeException {
    public UnauthorizedReviewAccessException(String message) {
        super(message);
    }
}

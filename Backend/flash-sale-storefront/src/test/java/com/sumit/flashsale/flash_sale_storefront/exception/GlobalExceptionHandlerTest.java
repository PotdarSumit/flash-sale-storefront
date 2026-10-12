package com.sumit.flashsale.flash_sale_storefront.exception;



import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturn429WhenRateLimitIsExceeded() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        RateLimitExceededException exception =
                new RateLimitExceededException(
                        "Rate limit exceeded. Please try again later."
                );

        // Act
        ResponseEntity<Map<String, Object>> response =
                handler.handleRateLimitExceeded(exception);

        // Assert
        assertEquals(
                HttpStatus.TOO_MANY_REQUESTS,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(429, response.getBody().get("status"));
        assertEquals(
                "Too Many Requests",
                response.getBody().get("error")
        );
        assertEquals(
                "Rate limit exceeded. Please try again later.",
                response.getBody().get("message")
        );
    }
}


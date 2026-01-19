package org.example.vladtech.portfolio.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Arrange
        String errorMessage = "Portfolio not found with ID: p123";

        // Act
        PortfolioNotFoundException exception = new PortfolioNotFoundException(errorMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testThrowingException() {
        // Arrange
        String errorMessage = "Portfolio not found";

        // Act & Assert
        PortfolioNotFoundException exception = assertThrows(
            PortfolioNotFoundException.class,
            () -> {
                throw new PortfolioNotFoundException(errorMessage);
            }
        );

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithNullMessage() {
        // Act
        PortfolioNotFoundException exception = new PortfolioNotFoundException(null);

        // Assert
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        // Act
        PortfolioNotFoundException exception = new PortfolioNotFoundException("");

        // Assert
        assertEquals("", exception.getMessage());
    }

    @Test
    void testExceptionWithLongMessage() {
        // Arrange
        String longMessage = "Portfolio not found: " + "x".repeat(1000);

        // Act
        PortfolioNotFoundException exception = new PortfolioNotFoundException(longMessage);

        // Assert
        assertEquals(longMessage, exception.getMessage());
        assertTrue(exception.getMessage().length() > 100);
    }

    @Test
    void testExceptionWithFormattedMessage() {
        // Arrange
        String portfolioId = "p123";
        String message = String.format("Portfolio with ID '%s' was not found in the database", portfolioId);

        // Act
        PortfolioNotFoundException exception = new PortfolioNotFoundException(message);

        // Assert
        assertTrue(exception.getMessage().contains(portfolioId));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testMultipleExceptionsWithDifferentMessages() {
        // Act
        PortfolioNotFoundException exception1 = new PortfolioNotFoundException("Portfolio p1 not found");
        PortfolioNotFoundException exception2 = new PortfolioNotFoundException("Portfolio p2 not found");

        // Assert
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertTrue(exception1.getMessage().contains("p1"));
        assertTrue(exception2.getMessage().contains("p2"));
    }
}


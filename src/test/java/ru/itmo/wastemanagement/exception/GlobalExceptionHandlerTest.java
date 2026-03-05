package ru.itmo.wastemanagement.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesResourceNotFound() {
        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("x", "id", 1)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Not Found");
    }

    @Test
    void handlesBadRequest() {
        ResponseEntity<Map<String, Object>> response = handler.handleBadRequestException(
                new BadRequestException("bad")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "bad");
    }

    @Test
    void handlesConflict() {
        ResponseEntity<Map<String, Object>> response = handler.handleConflictException(
                new ConflictException("dup")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "Conflict");
    }

    @Test
    void handlesValidationException() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "obj");
        result.addError(new FieldError("obj", "name", "must not be blank"));
        result.addError(new FieldError("obj", "name", "duplicate ignored"));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(result);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Validation Failed");
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).containsEntry("name", "must not be blank");
    }

    @Test
    void handlesGenericException() {
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("message").toString()).contains("boom");
    }

    @Test
    void resourceNotFoundFactoryBuildsMessage() {
        ResourceNotFoundException ex = ResourceNotFoundException.of(String.class, "id", 7);
        assertThat(ex.getMessage()).isEqualTo("String not found with id: '7'");
    }
}

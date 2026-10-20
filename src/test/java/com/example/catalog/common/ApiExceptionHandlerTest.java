package com.example.catalog.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiExceptionHandler Unit Tests")
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @DisplayName("should map ResponseStatusException to ProblemDetail with appropriate status and reason")
    void shouldMapResponseStatusExceptionToProblemDetail() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");

        ProblemDetail problemDetail = handler.handleStatus(exception);

        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getDetail()).isEqualTo("Product not found");
    }

    @Test
    @DisplayName("should map MethodArgumentNotValidException to ProblemDetail with field error list")
    void shouldMapMethodArgumentNotValidExceptionToProblemDetailWithFieldErrors() throws Exception {
        Object dummyTarget = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(dummyTarget, "createRequest");
        bindingResult.addError(new FieldError("createRequest", "name", "must not be blank"));
        bindingResult.addError(new FieldError("createRequest", "price", "must be greater than or equal to 0.00"));

        org.springframework.core.MethodParameter methodParameter = org.mockito.Mockito
                .mock(org.springframework.core.MethodParameter.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ProblemDetail problemDetail = handler.handleValidation(exception);

        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getDetail()).isEqualTo("Request validation failed");
        assertThat(problemDetail.getProperties()).containsKey("errors");

        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) problemDetail.getProperties().get("errors");
        assertThat(errors).containsExactly(
                "name: must not be blank",
                "price: must be greater than or equal to 0.00");
    }
}

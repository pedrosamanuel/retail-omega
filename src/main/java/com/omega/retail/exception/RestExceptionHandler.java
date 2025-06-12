package com.omega.retail.exception;

import com.omega.retail.dto.dtos.ApiErrorDTO;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice()
public class RestExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorDTO> handleRuntimeException(RuntimeException ex) {
        ApiErrorDTO error = new ApiErrorDTO(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleEntityNotFound(EntityNotFoundException ex) {
        ApiErrorDTO error = new ApiErrorDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGeneralException(Exception ex) {
        ApiErrorDTO error = new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno del servidor");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

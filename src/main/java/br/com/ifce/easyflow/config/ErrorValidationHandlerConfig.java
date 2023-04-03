package br.com.ifce.easyflow.config;

import br.com.ifce.easyflow.service.exceptions.BadRequestException;
import br.com.ifce.easyflow.service.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorValidationHandlerConfig {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetails> handlerBadRequestException(BadRequestException ex) {

        return new ResponseEntity<>(ProblemDetails.builder()
                .detail(ex.getMessage())
                .title("Bad Request Exception, check the Documentation")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetails> handlerResourceNotFoundException(ResourceNotFoundException ex) {

        return new ResponseEntity<>(ProblemDetails.builder()
                .detail(ex.getMessage())
                .title("Not Found Exception, check the Documentation")
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(Instant.now())
                .build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationExceptionDetails> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        String fields = fieldErrors.stream()
                .map(FieldError::getField)
                .collect(Collectors.joining(", "));

        String fieldsMessage = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(ValidationExceptionDetails.builder()
                .fields(fields)
                .fieldsMessage(fieldsMessage)
                .detail("**")
                .title("Bad Request Exception, Invalid Field(s), check the Documentation")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(Instant.now())
                .build(), HttpStatus.BAD_REQUEST);
    }

}

package dev.sotoestevez.coffeeservice.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<?>> validationErrorHandler(ConstraintViolationException ex){
        List<String> errorsList = ex.getConstraintViolations().stream().map(Object::toString).collect(Collectors.toList());

        return new ResponseEntity<>(errorsList, HttpStatus.BAD_REQUEST);
    }

}

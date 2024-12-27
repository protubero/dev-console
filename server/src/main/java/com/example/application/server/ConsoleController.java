package com.example.application.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.protubero.devconsole.common.ConsoleItem;
import de.protubero.devconsole.common.LogItem;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;


@RestController
@RequestMapping("api")
public class ConsoleController {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleController.class);

    private final LogItemDatabase database;
    private AtomicInteger requestCount = new AtomicInteger();

    private ConsoleController(LogItemDatabase aDatabase) {
        this.database = aDatabase;
    }

    @PostMapping("/append")
    public void append(@Valid @RequestBody ConsoleItem item) throws Exception {
        logger.debug("Receiving console item {}", item);
        database.append(item);
    }

    @PostMapping("/log")
    public void append(@Valid @RequestBody LogItem item) throws Exception {
        logger.debug("Receiving log item {}", item);
        database.append(item);
    }

    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, String> handleValidationExceptions(
            ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String fieldName = cv.getPropertyPath().toString();
            String errorMessage = cv.getMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

}

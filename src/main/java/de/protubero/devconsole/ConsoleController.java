package de.protubero.devconsole;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.protubero.devconsole.model.ConsoleItem;
import de.protubero.devconsole.model.SessionInfo;
import de.protubero.devconsole.wsmodel.SocketHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;


@RestController
@RequestMapping("api")
public class ConsoleController {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleController.class);

    private final SocketHandler socketHandler;

    private ConsoleController(@Autowired SocketHandler aSocketHandler) {
        this.socketHandler = aSocketHandler;
    }


    @PostMapping("/describe")
    public void describe(@Valid @RequestBody SessionInfo sessionInfo) {
        socketHandler.sessionInfo(sessionInfo);
    }

    @PostMapping("/append")
    public void append(@Valid @RequestBody ConsoleItem item) throws Exception {
		logger.info(item.toString());
        socketHandler.append(item);
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

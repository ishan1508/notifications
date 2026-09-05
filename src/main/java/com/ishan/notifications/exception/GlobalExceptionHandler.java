package com.ishan.notifications.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleInvalidBody(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return validationProblem(errors, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ProblemDetail> handleInvalidMethodArgument(
            HandlerMethodValidationException exception, HttpServletRequest request) {
        Stream<MessageSourceResolvable> parameterErrors = exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream());
        List<String> errors = Stream.concat(parameterErrors, exception.getCrossParameterValidationResults().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .toList();
        return validationProblem(errors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<String> errors = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return validationProblem(errors, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameter",
                "'" + exception.getName() + "' has an invalid value",
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleUnreadableMessage(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request body", "The request body is malformed", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ProblemDetail> handleMissingParameter(
            MissingServletRequestParameterException exception, HttpServletRequest request) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Missing request parameter",
                "'" + exception.getParameterName() + "' is required",
                request);
    }

    @ExceptionHandler(InvalidCursorException.class)
    ResponseEntity<ProblemDetail> handleInvalidCursor(InvalidCursorException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid pagination cursor", exception.getMessage(), request);
    }

    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    ResponseEntity<ProblemDetail> handleNotFound(Exception exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unhandled request failure", exception);
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred", request);
    }

    private ResponseEntity<ProblemDetail> validationProblem(List<String> errors, HttpServletRequest request) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST, "Validation failed", "One or more request values are invalid", request);
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status, String title, String detail, HttpServletRequest request) {
        return ResponseEntity.status(status).body(createProblem(status, title, detail, request));
    }

    private ProblemDetail createProblem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}

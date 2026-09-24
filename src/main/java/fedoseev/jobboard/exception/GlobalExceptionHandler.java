package fedoseev.jobboard.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(ResourceNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, "Not found", e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResource(NoResourceFoundException e) {
        return build(HttpStatus.NOT_FOUND, "Not found", "Ресурс не найден");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("Некорректные данные запроса");
        return build(HttpStatus.BAD_REQUEST, "Method not valid", message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException e) {
        return build(HttpStatus.BAD_REQUEST, "Bad request", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleUnreadable(HttpMessageNotReadableException e) {
        log.debug("Не удалось разобрать тело запроса", e);
        return build(HttpStatus.BAD_REQUEST, "Bad request", "Некорректное тело запроса");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return build(HttpStatus.BAD_REQUEST, "Bad request",
                "Параметр '" + e.getName() + "' имеет недопустимое значение");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingParam(MissingServletRequestParameterException e) {
        return build(HttpStatus.BAD_REQUEST, "Bad request",
                "Не передан обязательный параметр '" + e.getParameterName() + "'");
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponseDto> handlePropertyReference(PropertyReferenceException e) {
        return build(HttpStatus.BAD_REQUEST, "Bad request",
                "Неизвестное поле сортировки: " + e.getPropertyName());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException e) {
        String message = e.getMessage();
        if (message == null || message.startsWith("Access Denied")) {
            message = "Недостаточно прав";
        }
        return build(HttpStatus.FORBIDDEN, "Forbidden", message);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthentication(AuthenticationException e) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", "Неверный email или пароль");
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicate(DuplicateResourceException e) {
        return build(HttpStatus.CONFLICT, "Conflict", e.getMessage());
    }

    // две одновременные вставки проскакивают проверку exists и упираются в уникальный ключ
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException e) {
        log.debug("Нарушено ограничение БД", e);
        return build(HttpStatus.CONFLICT, "Conflict", "Запись уже существует или конфликтует с другими данными");
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(BadRequestException e){
        return build(HttpStatus.BAD_REQUEST, "Bad request", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public  ResponseEntity<ErrorResponseDto> handleGeneral(Exception e) {
        log.error("Необработанная ошибка", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Внутренняя ошибка сервера");
    }

    private ResponseEntity<ErrorResponseDto> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponseDto(error, message, LocalDateTime.now()));
    }
}

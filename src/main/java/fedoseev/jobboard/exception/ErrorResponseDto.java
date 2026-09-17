package fedoseev.jobboard.exception;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;


public record ErrorResponseDto(String error, String message, LocalDateTime timestamp) {}
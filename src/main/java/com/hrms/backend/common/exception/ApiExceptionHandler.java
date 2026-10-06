package com.hrms.backend.common.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hrms.backend.common.response.ErrorResponse;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException exception) {
		List<String> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getDefaultMessage())
				.toList();
		log.warn("Rejected invalid request");
		return ResponseEntity.badRequest()
				.body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid request.", errors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableRequest(HttpMessageNotReadableException exception) {
		log.warn("Rejected unreadable request");
		return ResponseEntity.badRequest()
				.body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid request.", List.of()));
	}

	@ExceptionHandler(AuthenticationFailedException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationFailure(AuthenticationFailedException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), exception.getMessage(), List.of()));
	}

	@ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleMissingAuthentication(
			AuthenticationCredentialsNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), exception.getMessage(), List.of()));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), exception.getMessage(), List.of()));
	}

	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ErrorResponse> handleInvalidBusinessRequest(InvalidRequestException exception) {
		return ResponseEntity.badRequest()
				.body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage(), List.of()));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage(), List.of()));
	}

	@ExceptionHandler(ResourceConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ResourceConflictException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ErrorResponse(HttpStatus.CONFLICT.value(), exception.getMessage(), List.of()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
		log.error("Unexpected error", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(
						HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"An unexpected error occurred.",
						List.of()));
	}
}

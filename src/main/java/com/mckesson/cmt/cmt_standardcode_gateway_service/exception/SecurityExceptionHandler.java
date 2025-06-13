package com.mckesson.cmt.cmt_standardcode_gateway_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for security-related exceptions
 */
@Slf4j
@ControllerAdvice
public class SecurityExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle authentication exceptions
     * @param ex The authentication exception
     * @param request The HTTP request
     * @return Response entity with error details
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.error("Authentication error: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Authentication failed: " + ex.getMessage());
        body.put("path", request.getRequestURI());
        
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Handle access denied exceptions
     * @param ex The access denied exception
     * @param request The HTTP request
     * @return Response entity with error details
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", "Access denied: Insufficient permissions");
        body.put("path", request.getRequestURI());
        
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
}
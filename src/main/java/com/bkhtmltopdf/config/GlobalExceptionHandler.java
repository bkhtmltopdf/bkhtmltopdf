package com.bkhtmltopdf.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication
class GlobalExceptionHandler {

    @ExceptionHandler(value = {NoHandlerFoundException.class, NoResourceFoundException.class}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object safeException(HttpServletRequest e) {
        return Map.of("message", e.getMethod() + " " + e.getRequestURI());
    }


    @ExceptionHandler(value = {RestClientException.class, MissingServletRequestPartException.class, HttpRequestMethodNotSupportedException.class}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object safeException(Exception e) {
        return Map.of("message", e.getMessage());
    }

    @ExceptionHandler(value = {Exception.class}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object exception(Exception e) {
        if (log.isErrorEnabled()) {
            log.error(e.getMessage(), e);
        }
        return Map.of("message", e.getMessage());
    }
}

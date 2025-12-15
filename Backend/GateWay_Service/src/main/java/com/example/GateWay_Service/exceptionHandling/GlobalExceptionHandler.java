package com.example.GateWay_Service.exceptionHandling;

import com.example.GateWay_Service.dtos.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFileTypeExecption.class)
    public ResponseEntity<ApiError> handleInvalidFileType(InvalidFileTypeExecption ex, HttpServletRequest request){

        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(),"Invalid file type", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> handleWebClientError(WebClientResponseException ex, HttpServletRequest request){

        String message = ex.getResponseBodyAsString();

        if (message == null || message.isBlank()){
            message = ex.getMessage();
        }

        ApiError error = new ApiError(ex.getStatusCode().value(),ex.getStatusText(),message,request.getRequestURI());

        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request){

        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error","An unexpected error occured.",request.getRequestURI());

        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

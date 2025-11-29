/**
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DExceptionHandler.java
 **/

package com.pucp.dp1.grupo4d.morapack.model.exception;

import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class G4DExceptionHandler {

    @ExceptionHandler(G4DException.class)
    public ResponseEntity<GenericResponse> handleException(G4DException e) {
        log.warn("G4D - WARN: {}", e.getMessage());
        return ResponseEntity.badRequest().body(new GenericResponse(false, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleException(Exception e) {
        log.error(String.format("G4D - ERROR: %s", e.getMessage()), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericResponse(false, "ERROR INTERNO: " + e.getMessage()));
    }
}

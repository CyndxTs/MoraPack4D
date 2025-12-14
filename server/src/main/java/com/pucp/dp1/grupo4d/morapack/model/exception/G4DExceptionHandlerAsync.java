package com.pucp.dp1.grupo4d.morapack.model.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class G4DExceptionHandlerAsync {

    public void handleException(String proccess, Throwable e) {
        if (e instanceof G4DException g4d) {
            log.warn(String.format("G4D[%s] - WARN: %s", proccess, e.getMessage()));
        } else {
            log.error(String.format("G4D[%s] - ERROR: %s", proccess, e.getMessage()), e);
        }
    }
}

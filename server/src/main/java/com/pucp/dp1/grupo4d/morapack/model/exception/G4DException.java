/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DException.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.exception;

public class G4DException extends Exception {

    public G4DException(String message) {
        super(message);
    }

    public G4DException(String message, Throwable cause) {
        super(message, cause);
    }
}

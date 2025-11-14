/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       GenericResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class GenericResponse {
    private String token;
    private Boolean success;
    private String message;

    public GenericResponse(Boolean success, String message) {
        this.token = G4D.Generator.getUniqueString("TOK");
        this.success = success;
        this.message = message;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Boolean isSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoadProgressResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadProgressResponse extends GenericResponse {
    Integer completed;
    Integer total;

    public LoadProgressResponse(Boolean success, String message) {
        super(success, message);
        this.completed = 0;
        this.total = 0;
    }

    public LoadProgressResponse(Boolean success, String message, Integer completed, Integer total) {
        super(success, message);
        this.completed = completed;
        this.total = total;
    }
}

/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ProgressPayload.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.payload;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressPayload {
    String proceso;
    Integer completado;
    Integer total;
}

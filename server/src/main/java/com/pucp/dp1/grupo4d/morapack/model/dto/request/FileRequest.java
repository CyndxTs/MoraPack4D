/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       FileRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRequest {
    String nombre;
    String directorio;
}

/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class G4DController {

    @GetMapping
    public String home() {
        return "SERVER ACTIVO \uD83D\uDDE3\uFE0F\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\n";
    }
}

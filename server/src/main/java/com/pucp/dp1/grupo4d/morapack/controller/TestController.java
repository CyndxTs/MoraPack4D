/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       TestController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String testear() {
        return "TESTING! \uD83D\uDDE3\uFE0F\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\n";
    }
}

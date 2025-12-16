/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.service.G4DService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class G4DController {
    private final G4DService g4dService;

    public G4DController(G4DService g4dService) {
        this.g4dService = g4dService;
    }

    @GetMapping
    public String morapack4D() {
        return "SERVER INICIADO \uD83D\uDDE3\uFE0F\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\n";
    }

    @PostMapping("/importation-init")
    public ResponseEntity<GenericResponse> iniciarImportacion(@RequestPart("file") MultipartFile file, @RequestPart("request") ImportFileRequest request) throws IOException {
        GenericResponse response = g4dService.iniciarImportacion(file, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/simulation-init")
    public ResponseEntity<GenericResponse> iniciarSimulacion(@RequestBody SimulationRequest request) {
        GenericResponse response = g4dService.iniciarSimulacion(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/simulation-stop")
    public ResponseEntity<GenericResponse> detenerSimulacion(@RequestBody TransactionRequest request) {
        GenericResponse response = g4dService.detenerSimulacion(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/operation-replanificate")
    public ResponseEntity<GenericResponse> replanificarOperacion(@RequestBody ReplanificationRequest request) {
        GenericResponse response = g4dService.replanificarOperacion(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exportation-init")
    public ResponseEntity<GenericResponse> iniciarExportacion(@RequestBody ExportationRequest request) {
        GenericResponse response = g4dService.iniciarExportacion(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exportation-preview")
    public ResponseEntity<Resource> previsualizarExportacion(@RequestBody FileRequest request) throws IOException {
        Path ruta = Paths.get(request.getRuta()).resolve(request.getNombre()).normalize().toAbsolutePath();
        if (!Files.exists(ruta)) {
            throw new G4DException(String.format("No se encontró el archivo'%s'", request.getNombre()));
        }
        if (!Files.isRegularFile(ruta)) {
            throw new G4DException("La ruta especificada no es un archivo válido.");
        }
        Resource resource = new UrlResource(ruta.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new G4DException(String.format("No se puede leer el archivo '%s'",  request.getNombre()));
        }
        String extension = request.getNombre().substring(request.getNombre().lastIndexOf(".") + 1).toLowerCase();
        String contentType = extension.equals("pdf") ? "application/pdf" : "text/plain";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + request.getNombre() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(Files.size(ruta))
                .body(resource);
    }

    @PostMapping("/exportation-download")
    public ResponseEntity<Resource> descargarExportacion(@RequestBody FileRequest request) throws IOException {
        Path ruta = Paths.get(request.getRuta()).resolve(request.getNombre()).normalize().toAbsolutePath();
        if (!Files.exists(ruta)) {
            throw new G4DException(String.format("No se encontró el archivo'%s'", request.getNombre()));
        }
        if (!Files.isRegularFile(ruta)) {
            throw new G4DException("La ruta especificada no es un archivo válido.");
        }
        Resource resource = new UrlResource(ruta.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new G4DException(String.format("No se puede leer el archivo '%s'",  request.getNombre()));
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + request.getNombre() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(ruta))
                .body(resource);
    }

    @PostMapping("/exportation-delete")
    public ResponseEntity<GenericResponse> eliminarExportacion(@RequestBody FileRequest request) throws IOException {
        Path ruta = Paths.get(request.getRuta()).resolve(request.getNombre()).normalize().toAbsolutePath();
        if (!Files.exists(ruta)) {
            throw new G4DException(String.format("No se encontró el archivo'%s'", request.getNombre()));
        }
        if (!Files.isRegularFile(ruta)) {
            throw new G4DException("La ruta especificada no es un archivo válido.");
        }
        Files.delete(ruta);
        return ResponseEntity.ok(new GenericResponse(true, "Archivo eliminado exitosamente!"));
    }
}

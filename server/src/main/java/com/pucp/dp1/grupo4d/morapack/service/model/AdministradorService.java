/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.repository.AdministradorRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;

    public AdministradorService(AdministradorRepository administradorRepository) {
        this.administradorRepository = administradorRepository;
    }

    public List<AdministradorEntity> findAll() {
        return administradorRepository.findAll();
    }

    public Optional<AdministradorEntity> findById(Integer id) {
        return administradorRepository.findById(id);
    }

    public Optional<AdministradorEntity> findByCodigo(String codigo) {
        return administradorRepository.findByCodigo(codigo);
    }

    public Optional<AdministradorEntity> findByCorreo(String correo) {
        return administradorRepository.findByCorreo(correo);
    }

    public AdministradorEntity save(AdministradorEntity administrador) {
        return administradorRepository.save(administrador);
    }

    public void deleteById(Integer id) {
        administradorRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return administradorRepository.existsById(id);
    }

    public boolean existsByCodigo(String codigo) {
        return administradorRepository.findByCodigo(codigo).isPresent();
    }

    public boolean existsByCorreo(String correo) {
        return administradorRepository.findByCorreo(correo).isPresent();
    }

    //
    public void importarDesdeArchivo(MultipartFile archivo) {
        // Declaracion de variables
        int cantUsu = 0;
        String linea;
        Scanner archivoSC = null, lineaSC;
        // Carga de datos
        try {
            G4D.Logger.logf("Cargando administradores desde '%s'..%n", archivo.getName());
            // Inicializaion del archivo y scanner
            archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            // Iterativa de lectura y carga
            while (archivoSC.hasNextLine()) {
                linea = archivoSC.nextLine().trim();
                // Inicializacion de scanner de linea
                lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}");
                AdministradorEntity administrador = new AdministradorEntity();
                administrador.setCodigo(lineaSC.next());
                administrador.setNombre(lineaSC.next());
                administrador.setCorreo(lineaSC.next());
                administrador.setContrasenia(lineaSC.next());
                save(administrador);
                cantUsu++;
                lineaSC.close();
            }
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", archivo.getName());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
        G4D.Logger.logf("[<] ADMINISTRADORES CARGADOS! ('%d' usuarios)%n", cantUsu);
    }
}

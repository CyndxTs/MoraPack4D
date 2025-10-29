/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.repository.ClienteRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteEntity> findAll() {
        return clienteRepository.findAll();
    }

    public Optional<ClienteEntity> findById(Integer id) {
        return clienteRepository.findById(id);
    }

    public Optional<ClienteEntity> findByCodigo(String codigo) {
        return clienteRepository.findByCodigo(codigo);
    }

    public Optional<ClienteEntity> findByCorreo(String correo) {
        return clienteRepository.findByCorreo(correo);
    }

    public ClienteEntity save(ClienteEntity cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteById(Integer id) {
        clienteRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return clienteRepository.existsById(id);
    }

    public boolean existsByCodigo(String codigo) {
        return clienteRepository.findByCodigo(codigo).isPresent();
    }

    public boolean existsByCorreo(String correo) {
        return clienteRepository.findByCorreo(correo).isPresent();
    }

    //
    public void importarDesdeArchivo(MultipartFile archivo) {
        // Declaracion de variables
        int cantUsu = 0;
        String linea;
        Scanner archivoSC = null, lineaSC;
        // Carga de datos
        try {
            G4D.Logger.logf("Cargando clientes desde '%s'..%n", archivo.getName());
            // Inicializaion del archivo y scanner
            archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            // Iterativa de lectura y carga
            while (archivoSC.hasNextLine()) {
                linea = archivoSC.nextLine().trim();
                // Inicializacion de scanner de linea
                lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}");
                ClienteEntity cliente = new ClienteEntity();
                cliente.setCodigo(lineaSC.next());
                cliente.setNombre(lineaSC.next());
                cliente.setCorreo(lineaSC.next());
                cliente.setContrasenia(lineaSC.next());
                save(cliente);
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
        G4D.Logger.logf("[<] CLIENTES CARGADOS! ('%d' usuarios)%n", cantUsu);
    }
}

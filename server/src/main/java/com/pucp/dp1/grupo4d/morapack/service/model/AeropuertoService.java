/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.repository.AeropuertoRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class AeropuertoService {

    private final AeropuertoRepository aeropuertoRepository;

    public AeropuertoService(AeropuertoRepository aeropuertoRepository) {
        this.aeropuertoRepository = aeropuertoRepository;
    }

    public List<AeropuertoEntity> findAll() {
        return aeropuertoRepository.findAll();
    }

    public Optional<AeropuertoEntity> findById(Integer id) {
        return aeropuertoRepository.findById(id);
    }

    public Optional<AeropuertoEntity> findByCodigo(String codigo) {
        return aeropuertoRepository.findByCodigo(codigo);
    }

    public Optional<AeropuertoEntity> findByAlias(String alias) {
        return aeropuertoRepository.findByAlias(alias);
    }

    public AeropuertoEntity save(AeropuertoEntity aeropuerto) {
        return aeropuertoRepository.save(aeropuerto);
    }

    public void deleteById(Integer id) {
        aeropuertoRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return aeropuertoRepository.existsById(id);
    }

    public boolean existsByCodigo(String codigo) {
        return aeropuertoRepository.findByCodigo(codigo).isPresent();
    }

    public boolean existsByAlias(String alias) {
        return aeropuertoRepository.findByAlias(alias).isPresent();
    }

    public void importarDesdeArchivo(MultipartFile archivo) {
        List<String> origCods = List.of("SPIM","EBCI","UBBB");
        List<AeropuertoEntity> origenes = new ArrayList<>();
        List<AeropuertoEntity> destinos = new ArrayList<>();
        String continente = "", linea;
        Scanner archivoSC = null, lineaSC;
        try {
            G4D.Logger.logf("Cargando aeropuertos desde '%s'..%n", archivo.getName());
            archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            while (archivoSC.hasNextLine()) {
                linea = archivoSC.nextLine().trim();
                if (linea.isEmpty()) continue;
                lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}");
                if (Character.isDigit(linea.charAt(0))) {
                    AeropuertoEntity aeropuerto = new AeropuertoEntity();
                    lineaSC.nextInt();
                    aeropuerto.setCodigo(lineaSC.next());
                    aeropuerto.setCiudad(lineaSC.next());
                    aeropuerto.setPais(lineaSC.next());
                    aeropuerto.setContinente(continente);
                    aeropuerto.setAlias(lineaSC.next());
                    aeropuerto.setHusoHorario(lineaSC.nextInt());
                    aeropuerto.setCapacidad(lineaSC.nextInt());
                    lineaSC.useDelimiter("\\s+");
                    lineaSC.next();
                    aeropuerto.setLatitudDMS(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    aeropuerto.setLatitudDEC(G4D.toLatDEC(aeropuerto.getLatitudDMS()));
                    lineaSC.next();
                    aeropuerto.setLongitudDMS(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    aeropuerto.setLongitudDEC(G4D.toLonDEC(aeropuerto.getLongitudDMS()));
                    if (origCods.contains(aeropuerto.getCodigo())) {
                        aeropuerto.setEsSede(true);
                        origenes.add(aeropuerto);
                    } else {
                        aeropuerto.setEsSede(false);
                        destinos.add(aeropuerto);
                    }
                } else continente = lineaSC.next();
                lineaSC.close();
            }
            origenes.forEach(this::save);
            destinos.forEach(this::save);
            G4D.Logger.logf("[<] AEROPUERTOS CARGADOS! ('%d' origenes | '%d' destinos)%n", origenes.size(), destinos.size());
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", archivo.getName());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
    }
}

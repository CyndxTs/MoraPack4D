/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4D_Formatter.java 
[**/

package pucp.grupo4d.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class G4D_Formatter {
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final String disp_ldt = "dd/MM/yyyy HH:mm";
    private static final String serv_ldt = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String disp_lt = "HH:mm";
    private static final String serv_lt = "HH:mm:ss";

    // Convertir latitud DMS a latitud DEC
    public static Double toLatDEC(String latDMS) {
        latDMS = latDMS.trim();
        Boolean negative = latDMS.endsWith("S");
        latDMS = latDMS.replaceAll("[NS]", "").trim();
        String[] parts = latDMS.split("[°'\"]+");
        Integer degrees = Integer.parseInt(parts[0].trim());
        Integer minutes = Integer.parseInt(parts[1].trim());
        Integer seconds = Integer.parseInt(parts[2].trim());
        Double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);
        return negative ? -decimal : decimal;
    }
    // Convertir latitud DEC a latitud DMS
    public static String toLatDMS(double latDEC) {
        String hemisphere = latDEC >= 0 ? "N" : "S";
        Double abs = Math.abs(latDEC);
        Integer degrees = abs.intValue();
        Double minutesDec = (abs - degrees) * 60;
        Integer minutes = minutesDec.intValue();
        Double secondsDec = (minutesDec - minutes) * 60;
        Integer seconds = (int) Math.round(secondsDec);
        return String.format("%02d° %02d' %02d\" %s", degrees, minutes, seconds, hemisphere);
    }
    // Convertir longitud DMS a longitud DEC
    public static Double toLonDEC(String lonDMS) {
        lonDMS = lonDMS.trim();
        Boolean negative = lonDMS.endsWith("W");
        lonDMS = lonDMS.replaceAll("[EW]", "").trim();
        String[] parts = lonDMS.split("[°'\"]+");
        Integer degrees = Integer.parseInt(parts[0].trim());
        Integer minutes = Integer.parseInt(parts[1].trim());
        Integer seconds = Integer.parseInt(parts[2].trim());
        Double decimal = degrees.doubleValue() + (minutes.doubleValue() / 60) + (seconds.doubleValue() / 3600);
        return negative ? -decimal : decimal;
    }
    // Convertir longitud DEC a longitud DMS
    public static String toLonDMS(Double lonDEC) {
        String hemisphere = lonDEC >= 0 ? "E" : "W";
        Double abs = Math.abs(lonDEC);
        Integer degrees = abs.intValue();
        Double minutesDec = (abs - degrees) * 60;
        Integer minutes = minutesDec.intValue();
        Double secondsDec = (minutesDec - minutes) * 60;
        Integer seconds = (int) Math.round(secondsDec);
        return String.format("%03d° %02d' %02d\" %s", degrees, minutes, seconds, hemisphere);
    }
    // Convertir 'DateTime' a 'DisplayString'
    public static String toDisplayString(LocalDateTime ldt) {
        return ldt.format(DateTimeFormatter.ofPattern(disp_ldt));
    }
    // Convertir 'DateTimeString' de BD a 'DateTime'
    public static LocalDateTime toDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(serv_ldt));
        } catch (Exception e) {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(disp_ldt));
        }
    }
    // Convertir 'Time' a 'DisplayString'
    public static String toDisplayString(LocalTime lt) {
        return lt.format(DateTimeFormatter.ofPattern(disp_lt));
    }
    // Convertir 'TimeString' a 'Time'
    public static LocalTime toTime(String timeString) {
        try {
            return LocalTime.parse(timeString, DateTimeFormatter.ofPattern(serv_lt));
        } catch (Exception e) {
            return LocalTime.parse(timeString, DateTimeFormatter.ofPattern(disp_lt));
        }
    }
    // Convertir 'Time' a 'DateTime'
    public static LocalDateTime toDateTime(LocalTime lt) {
        LocalDateTime ldt_ref = LocalDateTime.now();
        return ldt_ref.withHour(lt.getHour()).withMinute(lt.getMinute());
    }
    // Convertir 'Time' a 'DateTime'
    public static LocalDateTime toDateTime(LocalTime lt, LocalDateTime ldt_ref) {
        return ldt_ref.withHour(lt.getHour()).withMinute(lt.getMinute());
    }
    // Convertir 'Local' a 'UTC'
    public static LocalDateTime toUTC(LocalDateTime ldt, Integer gmt) {
        return ldt.minusHours(Long.valueOf(gmt));
    }
    // Convertir 'Local' a 'UTC'
    public static LocalTime toUTC(LocalTime lt, Integer gmt) {
        if(lt == null) return null;
        return lt.minusHours(Long.valueOf(gmt));
    }
    // Convertir 'UTC' a 'Local'
    public static LocalDateTime toLocal(LocalDateTime ldt, Integer gmt) {
        if(ldt == null) return null;
        return ldt.plusHours(Long.valueOf(gmt));
    }
    // Convertir 'UTC' a 'Local'
    public static LocalTime toLocal(LocalTime lt, Integer gmt) {
        return lt.plusHours(Long.valueOf(gmt));
    }
    // Calcular distancia geodesica
    public static Double calculateGeodesicDistance(Double origLat, Double origLon, Double destLat, Double destLon) {
        Double lat1 = Math.toRadians(origLat), lon1 = Math.toRadians(origLon);
        Double lat2 = Math.toRadians(destLat), lon2 = Math.toRadians(destLon);
        Double dLat = lat2 - lat1, dLon = lon2 - lon1;
        Double h = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
    }
    // Calcular horas transcurridas de origen a destino
    public static Double calculateElapsedHours(LocalTime ltOrig,LocalTime ltDest) {
        return Duration.between(ltOrig,ltDest).toMinutes() / 60.0;
    }
    // Calcular horas transcurridas de origen a destino
    public static Double calculateElapsedHours(LocalDateTime ldtOrig, LocalDateTime ldtDest) {
        return Duration.between(ldtOrig,ldtDest).toMinutes() / 60.0;
    }
    // Generar identificador unico
    public static String generateIdentifier(String prefix) {
        Long millis = System.currentTimeMillis();
        String base36Millis = Long.toString(millis, 36);
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        BigInteger uuidNum = new BigInteger(uuid, 16);
        String base36Uuid = uuidNum.toString(36);
        return prefix + "-" + base36Millis + base36Uuid;
    }
    // Obtener charset de archivo
    public static Charset getFileCharset(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bom = new byte[3];
            Integer n = fis.read(bom, 0, bom.length);

            if (n >= 2) {
                if ((bom[0] & 0xFF) == 0xFF && (bom[1] & 0xFF) == 0xFE) {
                    return StandardCharsets.UTF_16LE; // UTF-16 Little Endian
                }
                if ((bom[0] & 0xFF) == 0xFE && (bom[1] & 0xFF) == 0xFF) {
                    return StandardCharsets.UTF_16BE; // UTF-16 Big Endian
                }
            }
            if (n == 3) {
                if ((bom[0] & 0xFF) == 0xEF && (bom[1] & 0xFF) == 0xBB && (bom[2] & 0xFF) == 0xBF) {
                    return StandardCharsets.UTF_8; // UTF-8 with BOM
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return StandardCharsets.UTF_8; // default UTF-8
    }
    // Imprimir simbolos en linea en archivo
    public static void printFullLine(PrintWriter writer, char symbol, int lineDim) {
        for (int i = 0; i < lineDim; i++) writer.print(symbol);
        writer.println();
    }
    // Imprimir simbolos en linea en archivo
    public static void printFullLine(PrintWriter writer, char symbol, int lineDim, int offset) {
        int limit = lineDim - 2 * offset;
        for (int i = 0; i < offset; i++) writer.print(" ");
        for (int i = 0; i < limit; i++) writer.print(symbol);
        writer.println();
    }
    // Imprimir cadena centrada respecto a medida
    public static void printCentered(PrintWriter writer, int lineDim, String text) {
        writer.printf("%" + ((lineDim + text.length()) / 2) + "s%n", text);
    }
    // Imprimir cadena centrada y enmarcada respecto a medida
    public static void printCentered(PrintWriter writer, int lineDim, String text, String border) {
        if (border.length() != 2) throw new IllegalArgumentException("The border must have 2 characters");
        int space = (lineDim - text.length())/2;
        writer.println(String.valueOf(border.charAt(0)).repeat(space - 1) + text + String.valueOf(border.charAt(1)).repeat(space - 1));
    }
    // Clase agrupadora de 'Integer' para pasar por referencia
    public static class IntegerWrapper {
        public Integer value;

        public IntegerWrapper() {
            this.value = 0;
        }

        public IntegerWrapper(Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(this.value);
        }
    }
    // Interfaz de replicacion para modelo
    public static interface Replicable<T> {
        T replicar();
    } 
    // Replicar lista 'modelo' en otra
    public static <T extends Replicable<T>> List<T> replicar(List<T> lista) {
        List<T> replica = new ArrayList<>();
        for (T e : lista) {
            T ne = e.replicar();
            replica.add(ne);
        }
        return replica;
    }
}

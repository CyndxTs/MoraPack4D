package pucp.grupo4d.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class G4D_Formatter {
    private static final double RADIO_TIERRA_KM = 6371.0;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

    // Convertir Latitud DMS a Latitud Decimal
    public static double toLatDEC(String dms) {
        dms = dms.trim();
        boolean negativo = dms.endsWith("S"); // latitud sur negativa
        dms = dms.replaceAll("[NS]", "").trim();
        String[] partes = dms.split("[°'\"]+");
        int grados = Integer.parseInt(partes[0].trim());
        int minutos = Integer.parseInt(partes[1].trim());
        int segundos = Integer.parseInt(partes[2].trim());
        double decimal = grados + (minutos / 60.0) + (segundos / 3600.0);
        return negativo ? -decimal : decimal;
    }
    // Convertir Latitud Decimal a Latitud DMS
    public static String toLatDMS(double decimal) {
        String hemisferio = decimal >= 0 ? "N" : "S";
        double abs = Math.abs(decimal);
        int grados = (int) abs;
        double minutosDec = (abs - grados) * 60;
        int minutos = (int) minutosDec;
        double segundosDec = (minutosDec - minutos) * 60;
        int segundos = (int) Math.round(segundosDec);

        return String.format("%02d° %02d' %02d\" %s", grados, minutos, segundos, hemisferio);
    }
    // Convertir Longitud DMS a Longitud Decimal
    public static double toLonDEC(String dms) {
        dms = dms.trim();
        boolean negativo = dms.endsWith("W"); // longitud oeste negativa
        dms = dms.replaceAll("[EW]", "").trim();

        String[] partes = dms.split("[°'\"]+");
        int grados = Integer.parseInt(partes[0].trim());
        int minutos = Integer.parseInt(partes[1].trim());
        int segundos = Integer.parseInt(partes[2].trim());

        double decimal = grados + (minutos / 60.0) + (segundos / 3600.0);
        return negativo ? -decimal : decimal;
    }
    // Convertir Longitud Decimal a Longitud DMS
    public static String toLonDMS(double decimal) {
        String hemisferio = decimal >= 0 ? "E" : "W";
        double abs = Math.abs(decimal);

        int grados = (int) abs;
        double minutosDec = (abs - grados) * 60;
        int minutos = (int) minutosDec;
        double segundosDec = (minutosDec - minutos) * 60;
        int segundos = (int) Math.round(segundosDec);

        return String.format("%03d° %02d' %02d\" %s", grados, minutos, segundos, hemisferio);
    }
    // Obtener Charset de Archivo
    public static Charset getFileCharset(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bom = new byte[3];
            int n = fis.read(bom, 0, bom.length);

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
                    return StandardCharsets.UTF_8; // UTF-8 con BOM
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return StandardCharsets.UTF_8; // UTF-8 default
    }
    // Imprimir Simbolos en Linea en Archivo
    public static void imprimirLinea(PrintWriter writer, char simbolo, int medida) {
        for (int i = 0; i < medida; i++) writer.print(simbolo);
        writer.println();
    }
    // Imprimir Simbolos en Linea en Archivo
    public static void imprimirLinea(PrintWriter writer, char simbolo, int medida, int offset) {
        int limite = medida - 2*offset;
        for (int i = 0; i < offset; i++) writer.print(" ");
        for (int i = 0; i < limite; i++) writer.print(simbolo);
        writer.println();
    }
    // Imprimir Cadena Centrada Respecto a Medida
    public static void imprimirCentrado(PrintWriter writer, int dimLinea, String cadena) {
        writer.printf("%" + ((dimLinea + cadena.length())/2) + "s%n", cadena);
    }
    // Imprimir Cadena Centrada y Enmarcada Respecto a Medida
    public static void imprimirCentrado(PrintWriter w,int dim,String txt,String marco) {
        if(marco.length()!=2) throw new IllegalArgumentException("El marco debe tener 2 caracteres");
        int esp=(dim-txt.length())/2;
        w.println(String.valueOf(marco.charAt(0)).repeat(esp-1) + " " + txt + " " + String.valueOf(marco.charAt(1)).repeat(esp-1));
    }
    // Calcular Distancia Con Coordenadas
    public static double calcularDistancia(double origLat, double origLon, double destLat, double destLon) {
        double lat1 = Math.toRadians(origLat), lon1 = Math.toRadians(origLon);
        double lat2 = Math.toRadians(destLat), lon2 = Math.toRadians(destLon);
        double dLat = lat2 - lat1, dLon = lon2 - lon1;
        double h = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * 6371.0 * Math.asin(Math.sqrt(h));
    }
    //
    public static double toDEC_Hour(String time) {
        LocalTime t = LocalTime.parse(time, tf);
        return t.getHour() + t.getMinute() / 60.0;
    }
    //
    public static String toTimeString(String dateTime) {
        LocalDateTime ldt = LocalDateTime.parse(dateTime, dtf);
        if(ldt.getSecond() > 0) ldt = ldt.withSecond(0).plusMinutes(1);
        return ldt.format(tf);
    }
    public static String toDateTimeString(LocalDateTime ldt) {
        return ldt.format(dtf);
    }
}

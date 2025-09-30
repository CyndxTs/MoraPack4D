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
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class G4D_Util {
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
        Double duration = Duration.between(ltOrig,ltDest).toMinutes() / 60.0;
        if(duration < 0) duration += 24.0;
        return duration;
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

        public void increment() {
            this.value++;
        }

        public void increment(int value) {
            this.value += value;
        }

        public void decrement() {
            this.value--;
        }

        public void decrement(int value) {
            this.value-=value;
        }

        @Override
        public String toString() {
            return String.valueOf(this.value);
        }
    }
    // Enum que contiene cadenas 'ANSI' para el uso de la consola
    public static class Logger {
        private static enum Action {
            UP("U","A"),
            DOWN("D","B"),
            RIGHT("R","C"),
            LEFT("L","D"),
            CLEAR("C","P"),
            CLEAR_LINE("Cl","K"),
            CLEAR_SCREEN("Cs","J"),
            POSITION_LINE("Pl","G");
            
            private final String id;
            private final String ansiCode;

            Action(String id,String ansiCode) {
                this.id = id;
                this.ansiCode = ansiCode;
            }
            //
            public static String delete(int numChars) {
                return getAnsiString("D",numChars) + getAnsiString("P",numChars);
            }
            //
            public static String delete_line() {
                return getAnsiString("K",2) + getAnsiString("G",1);
            }
            //
            public static String to_ansi(String actionId,int mode) {
                for(Action action : Action.values()) {
                    if(actionId.compareTo(action.id) == 0) return getAnsiString(action.ansiCode, mode);
                }
                return "";
            }
            //
            private static String getAnsiString(String ansiCode,int mode) {
                return "\u001B[" + mode + ansiCode;
            }
        }

        private static enum Color {
            RESET("Rs",0),
            RED("Rd",31),
            GREEN("Gn",32),
            BLUE("Bl",34),
            YELLOW("Yl",33),
            CYAN("Cy",36),
            PURPLE("Pr",35),
            WHITE("Wh",37),
            BLACK("Bk",30);

            private final String id;
            private final int ansiCode;

            Color(String id,int ansiCode) {
                this.id = id;
                this.ansiCode = ansiCode;
            }
            //
            public static String set_color(String colorId) {
                return to_ansi(colorId);
            }
            //
            public static String reset_color() {
                return getAnsiString(0);
            }
            //
            public static String to_ansi(String colorId){
                for(Color color : Color.values()) {
                    if(colorId.compareTo(color.id) == 0) return getAnsiString(color.ansiCode);
                }
                return "";
            }
            //
            private static String getAnsiString(int ansiCode) {
                return "\u001B[" + ansiCode + "m";
            }            
        }

        private static final java.util.logging.Logger logger;
        private static final ConsoleHandler handler;
        private static boolean enabled;

        static {
            logger = java.util.logging.Logger.getLogger("G4D_Logger");
            handler = new ConsoleHandler();
            enabled = true;
            // Configuracion para que solo se imprima el mensaje
            handler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return record.getMessage();
                }
            });
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
            logger.setLevel(Level.INFO);
        }
        // Función para imprimir un mensaje (nivel INFO por defecto)
        public static void log(String msg) {
            logger.info(String.format("%s",msg));
        }
        //
        public static void logln(String msg) {
            logger.info(String.format("%s%n",msg));
        }
        //
        public static void logf(String msgFormat, Object... args) {
            logger.info(String.format(msgFormat, args));
        }
        //
        public static void log_err(String msg) {
            G4D_Util.Logger.Color.set_color("Rd");
            log(msg);
            G4D_Util.Logger.Color.set_color("Rs");
        }
        //
        public static void logln_err(String msg) {
            G4D_Util.Logger.Color.set_color("Rd");
            logln(msg);
            G4D_Util.Logger.Color.set_color("Rs");
        }
        //
        public static void logf_err(String msgFormat, Object... args) {
            G4D_Util.Logger.Color.set_color("Rd");
            logf(msgFormat, args);
            G4D_Util.Logger.Color.set_color("Rs");
        }
        //
        public static void delete(int numChars) {
            logger.info(G4D_Util.Logger.Action.delete(numChars));
        }
        //
        public static void delete_current_line() {
            logger.info(G4D_Util.Logger.Action.delete_line());
        }
        //
        public static void delete_upper_line() {
            custom_action("U1Cl2");
        }
        //
        public static void delete_lines(int numLines) {
            for(int i = 0;i < numLines - 1;i++) {
                logger.info(G4D_Util.Logger.Action.delete_line());
                logger.info(G4D_Util.Logger.Action.to_ansi("U",1));
            }
            logger.info(G4D_Util.Logger.Action.delete_line());
        }
        //
        public static void set_color(String colorId) {
            logger.info(G4D_Util.Logger.Color.set_color(colorId));
        }
        //
        public static void reset_color() {
            logger.info(G4D_Util.Logger.Color.reset_color());
        }
        //
        public static void custom_action(String actions) {
            String ansiString = "";
            Pattern pattern = Pattern.compile("[A-Z][a-z]?\\d*");
            Matcher matcher = pattern.matcher(actions);
            while (matcher.find()) {
                int mode;
                String action = matcher.group();
                String id = String.valueOf(action.charAt(0));
                if(action.length() > 1) {
                    if(Character.isLowerCase(action.charAt(1))) {
                        id += action.charAt(1);
                        if(action.length() > 2) mode = Integer.valueOf(action.substring(2));
                        else mode = 1;
                    } else mode = Integer.valueOf(action.substring(1));
                } else mode = 1;
                ansiString += G4D_Util.Logger.Action.to_ansi(id, mode);
            }
            logger.info(ansiString);
        }
        // Función para activar/desactivar logs
        public static void toggle_log() {
            enabled = !enabled;
            logger.setLevel(enabled ? Level.INFO : Level.OFF);
        }
    }
}

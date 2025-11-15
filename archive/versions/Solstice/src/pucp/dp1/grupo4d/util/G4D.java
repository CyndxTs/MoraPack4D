/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4D.java 
[**/

package pucp.dp1.grupo4d.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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

public class G4D {
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final DateTimeFormatter dtf_display = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter dtf_server = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter tf_display = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter tf_server = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Obtener latitud 'DEC' a partir de latitud 'DMS'
    public static double toLatDEC(String latDMS) {
        latDMS = latDMS.trim();
        boolean negative = latDMS.endsWith("S");
        latDMS = latDMS.replaceAll("[NS]", "").trim();
        String[] parts = latDMS.split("[°'\"]+");
        int degrees = Integer.parseInt(parts[0].trim());
        int minutes = Integer.parseInt(parts[1].trim());
        int seconds = Integer.parseInt(parts[2].trim());
        double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);
        return negative ? -decimal : decimal;
    }
    // Obtener latitud 'DMS' a partir de latitud 'DEC'
    public static String toLatDMS(double latDEC) {
        String hemisphere = latDEC >= 0 ? "N" : "S";
        double abs = Math.abs(latDEC);
        int degrees = (int) abs;
        double minutesDec = (abs - degrees) * 60;
        int minutes = (int) minutesDec;
        double secondsDec = (minutesDec - minutes) * 60;
        int seconds = (int) Math.round(secondsDec);
        return String.format("%02d° %02d' %02d\" %s", degrees, minutes, seconds, hemisphere);
    }
    // Obtener longitud 'DEC' a partir de longitud 'DMS'
    public static double toLonDEC(String lonDMS) {
        lonDMS = lonDMS.trim();
        boolean negative = lonDMS.endsWith("W");
        lonDMS = lonDMS.replaceAll("[EW]", "").trim();
        String[] parts = lonDMS.split("[°'\"]+");
        int degrees = Integer.parseInt(parts[0].trim());
        int minutes = Integer.parseInt(parts[1].trim());
        int seconds = Integer.parseInt(parts[2].trim());
        double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);
        return negative ? -decimal : decimal;
    }
    // Obtener longitud 'DMS' a partir de longitud 'DEC'
    public static String toLonDMS(double lonDEC) {
        String hemisphere = lonDEC >= 0 ? "E" : "W";
        double abs = Math.abs(lonDEC);
        int degrees = (int) abs;
        double minutesDec = (abs - degrees) * 60;
        int minutes = (int) minutesDec;
        double secondsDec = (minutesDec - minutes) * 60;
        int seconds = (int) Math.round(secondsDec);
        return String.format("%03d° %02d' %02d\" %s", degrees, minutes, seconds, hemisphere);
    }
    // Obtener 'DisplayString' a partir de 'DateTime'
    public static String toDisplayString(LocalDateTime dt) {
        return dt.format(dtf_display);
    }
    // Obtener 'ServerString' a partir de 'DateTime'
    public static String toServerString(LocalDateTime dt) {
        return dt.format(dtf_server);
    }
    // Obtener 'DateTime' a partir de 'DateTimeString'
    public static LocalDateTime toDateTime(String dts) {
        try {
            return LocalDateTime.parse(dts, dtf_display);
        } catch (Exception e) {
            return LocalDateTime.parse(dts, dtf_server);
        }
    }
    // Obtener 'DateTime' a partir de 'Time'
    public static LocalDateTime toDateTime(LocalTime t) {
        return LocalDateTime.now().withHour(t.getHour()).withMinute(t.getMinute()).withSecond(t.getSecond());
    }
    // Obtener 'DateTime' a partir de 'Time' con referencia de otro 'DateTime'
    public static LocalDateTime toDateTime(LocalTime t, LocalDateTime dt_ref) {
        return dt_ref.withHour(t.getHour()).withMinute(t.getMinute()).withSecond(t.getSecond());
    }
    // Obtener 'DisplayString' a partir de 'Time'
    public static String toDisplayString(LocalTime t) {
        return t.format(tf_display);
    }
    // Obtener 'ServerString' a partir de 'Time'
    public static String toServerString(LocalTime t) {
        return t.format(tf_server);
    }
    // Obtener 'Time' a partir de 'TimeString'
    public static LocalTime toTime(String ts) {
        try {
            return LocalTime.parse(ts, tf_display);
        } catch (Exception e) {
            return LocalTime.parse(ts, tf_server);
        }
    }
    // Obtener 'Time' a partir de 'DateTime'
    public static LocalTime toTime(LocalDateTime dt) {
        return dt.toLocalTime();
    }
    //
    public static LocalDate toDate(Integer tINT) {
        int year = tINT / 10000;
        int month = (tINT / 100) % 100;
        int day = tINT % 100;
        return LocalDate.of(year, month, day);
    }
    // Obtener 'DateTime UTC' a partir de 'DateTime Local'
    public static LocalDateTime toUTC(LocalDateTime dt, Integer gmt) {
        return dt.minusHours(Long.valueOf(gmt));
    }
    // Obtener 'DateTime Local' a partir de 'DateTime UTC'
    public static LocalDateTime toLocal(LocalDateTime dt, Integer gmt) {
        return dt.plusHours(Long.valueOf(gmt));
    }
    // Obtener 'Time UTC' a partir de 'Time Local'
    public static LocalTime toUTC(LocalTime t, Integer gmt) {
        return t.minusHours(Long.valueOf(gmt));
    }
    // Obtener 'Time Local' a partir de 'Time UTC'
    public static LocalTime toLocal(LocalTime t, Integer gmt) {
        return t.plusHours(Long.valueOf(gmt));
    }
    // Obtener 'DateTimeRange' a partir de 2 'Time' con referencia a 'DateTime'
    public static LocalDateTime[] getDateTimeRange(LocalTime t_departure, LocalTime t_arrival, LocalDateTime dt_ref) {
        LocalDateTime departure = G4D.toDateTime(t_departure, dt_ref);
        LocalDateTime arrival = G4D.toDateTime(t_arrival, dt_ref);
        if(arrival.isBefore(departure)) {
            arrival = arrival.plusDays(1);
        }
        if(departure.isBefore(dt_ref)) {
            departure = departure.plusDays(1);
            arrival = arrival.plusDays(1);
        }
        return new LocalDateTime[]{departure, arrival};
    }
    // Obtener distancia 'Geodésica'
    public static double getGeodesicDistance(double origLat, double origLon, double destLat, double destLon) {
        double lat1 = Math.toRadians(origLat);
        double lon1 = Math.toRadians(origLon);
        double lat2 = Math.toRadians(destLat);
        double lon2 = Math.toRadians(destLon);
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double h = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
    }
    // Obtener tiempo transcurrido en 'horas' entre 2 'DateTime'
    public static double getElapsedHours(LocalDateTime dtOrig, LocalDateTime dtDest) {
        return Duration.between(dtOrig, dtDest).toMinutes() / 60.0;
    }
    // Obtener tiempo transcurrido en 'horas' entre 2 'Time'
    public static double getElapsedHours(LocalTime tOrig, LocalTime tDest) {
        double duration = Duration.between(tOrig, tDest).toMinutes() / 60.0;
        if (duration < 0) duration += 24.0;
        return duration;
    }
    // Obtener 'UniqueString' a partir de prefijo
    public static String getUniqueString(String prefix) {
        long millis = System.currentTimeMillis();
        String base36Millis = Long.toString(millis, 36);
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        BigInteger uuidNum = new BigInteger(uuid, 16);
        String base36Uuid = uuidNum.toString(36);
        return prefix + "-" + base36Millis + base36Uuid;
    }
    // Obtener 'UniqueString' como nombre de cliente
    public static String getUniqueName() {
        return "Candy Tasty Hyper Candy";
    }
    // Obtener 'Charset' de un archivo
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
    //
    public static class IntegerWrapper {
        public int value;

        public IntegerWrapper() {
            this.value = 0;
        }

        public IntegerWrapper(int value) {
            this.value = value;
        }
        //
        public void increment() {
            this.value++;
        }
        //
        public void increment(int value) {
            this.value += value;
        }
        //
        public void decrement() {
            this.value--;
        }
        //
        public void decrement(int value) {
            this.value-=value;
        }
        //
        @Override
        public String toString() {
            return String.valueOf(this.value);
        }
        //
        public int compareTo(IntegerWrapper other) {
            return Integer.compare(this.value, other.value);
        }
    }
    //
    public static class Printer {
        private static PrintWriter pw;
        
        //
        public static void open(String archDIR) {
            close();
            try {
                FileWriter arch = new FileWriter(archDIR);
                pw = new PrintWriter(arch);
            } catch (IOException e) {
                pw = null;
                e.printStackTrace();
            }
        }
        //
        public static void flush() {
            if(pw != null) {
                pw.flush();
            }
        }
        //
        public static void close() {
            if (pw != null) {
                pw.close();
                pw = null;
            }
        }
        //
        public static void print(String text) {
            pw.print(text);
        }
        //
        public static void println(String text) {
            pw.println(text);
        }
        //
        public static void println() {
            pw.println();
        }
        //
        public static void printf(String text, Object... args) {
            pw.printf(text, args);
        }
        //
        public static void fill_line(char symbol, int lineDim) {
            for (int i = 0; i < lineDim; i++) pw.print(symbol);
            pw.println();
        }
        //
        public static void fill_line(char symbol, int lineDim, int offset) {
            int limit = lineDim - 2 * offset;
            for (int i = 0; i < offset; i++) pw.print(" ");
            for (int i = 0; i < limit; i++) pw.print(symbol);
            pw.println();
        }
        //
        public static void print_centered(String text, int lineDim) {
            pw.printf("%" + ((lineDim + text.length()) / 2) + "s%n", text);
        }
        //
        public static void print_centered(String text, int lineDim, String border) {
            int space = (lineDim - text.length())/2;
            pw.println(String.valueOf(border.charAt(0)).repeat(space - 1) + text + String.valueOf(border.charAt(1)).repeat(space - 1));
        }
    }
    //
    public static class Logger {
        //
        private static enum Action {
            UP("U","A"),
            DOWN("D","B"),
            RIGHT("R","C"),
            LEFT("L","D"),
            CLEAR("C","P"),
            CLEAR_LINE("Cl","K"),
            CLEAR_SCREEN("Cs","J"),
            POSITION_LINE("Pl","G"),
            POSITION_SCREEN("Ps","H");
            
            private final String id;
            private final String code;

            Action(String id,String code) {
                this.id = id;
                this.code = code;
            }
            //
            public static String delete(int numChars) {
                return getAnsiString("D",numChars) + getAnsiString("P",numChars);
            }
            //
            public static String delete_current_line() {
                return getAnsiString("K",2) + getAnsiString("G",1);
            }
            //
            public static String to_ansi(String actionId, int mode) {
                for(Action action : Action.values()) {
                    if(actionId.compareTo(action.id) == 0) {
                        return getAnsiString(action.code, mode);
                    }
                }
                return "";
            }
            //
            private static String getAnsiString(String code, int mode) {
                return "\u001B[" + mode + code;
            }
        }
        //
        private static enum Color {
            RESET("0","0"),
            RED("R","31"),
            GREEN("G","32"),
            YELLOW("Y","33"),
            BLUE("B","34"),
            PURPLE("P","35");

            private final String id;
            private final String code;

            Color(String id, String code) {
                this.id = id;
                this.code = code;
            }
            //
            public static String set_color(String colorId) {
                return to_ansi(colorId);
            }
            //
            public static String reset_color() {
                return getAnsiString("0");
            }
            //
            public static String to_ansi(String colorId){
                for(Color color : Color.values()) {
                    if(colorId.compareTo(color.id) == 0) {
                        return getAnsiString(color.code);
                    }
                }
                return "";
            }
            //
            private static String getAnsiString(String code) {
                return "\u001B[" + code + "m";
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
        //
        public static void toggle_log() {
            enabled = !enabled;
            logger.setLevel(enabled ? Level.INFO : Level.OFF);
        }
        //
        public static void log(String msg) {
            logger.info(String.format(msg));
        }
        //
        public static void logln(String msg) {
            logger.info(String.format(msg + "%n"));
        }
        //
        public static void logln() {
            logger.info(String.format("%n"));
        }
        //
        public static void logf(String msgFormat, Object... args) {
            logger.info(String.format(msgFormat, args));
        }
        //
        public static void log_err(String msg) {
            set_color("R");
            logger.severe(String.format(msg));
            reset_color();
        }
        //
        public static void logln_err(String msg) {
            set_color("R");
            logger.severe(String.format(msg + "%n"));
            reset_color();
        }
        //
        public static void logf_err(String msgFormat, Object... args) {
            set_color("R");
            logger.severe(String.format(msgFormat, args));
            reset_color();
        }
        //
        public static void delete(int numChars) {
            logger.info(Action.delete(numChars));
        }
        //
        public static void delete_current_line() {
            logger.info(Action.delete_current_line());
        }
        //
        public static void delete_upper_line() {
            logger.info(getCustomAction("U1Cl2"));
        }
        //
        public static void delete_lines(int numLines) {
            logger.info(Action.delete_current_line());
            for(int i = 0;i < numLines - 1;i++) {
                logger.info(Action.to_ansi("U",1));
                logger.info(Action.delete_current_line());
            }
        }
        //
        public static void set_color(String colorId) {
            logger.info(Color.set_color(colorId));
        }
        //
        public static void reset_color() {
            logger.info(Color.reset_color());
        }
        //
        private static String getCustomAction(String actions) {
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
                        if(action.length() > 2) {
                            mode = Integer.valueOf(action.substring(2));
                        } else mode = 1;
                    } else mode = Integer.valueOf(action.substring(1));
                } else mode = 1;
                ansiString += Action.to_ansi(id, mode);
            }
            return ansiString;
        }
        //
        public static class Stats {
            private static Instant start;
            private static Instant end;
            private static long duration;
            public static int posPed = 0;
            public static int numPed = 1;
            public static int totalPed = 0;
            public static int posProd = 0;
            public static int numProd = 1;
            public static int totalProd = 0;

            public static void set_start() {
                start = Instant.now();
            }

            public static void set_end() {
                end = Instant.now();
            }

            public static void set_duration() {
                duration = Duration.between(start, end).toNanos();
            }

            public static double get_mean_time_by_ped() {
                return duration/(1000000.0*posPed);
            }

            public static double get_mean_time_by_prod() {
                return duration/(1000.0*posProd);
            }

            public static void log_ped_stat() {
                G4D.Logger.logf("[#] TOTAL DE PEDIDOS ATENDIDOS: %d de' %d'%n",posPed,totalPed);
                G4D.Logger.logf("[#] TIEMPO PROMEDIO DE ATENCION POR PEDIDO: %.3f ms.%n",get_mean_time_by_ped());
            }

            public static void log_prod_stat() {
                G4D.Logger.logf("[#] TOTAL DE PRODUCTOS ENRUTADOS: %d de '%d'%n",posProd,totalProd);
                G4D.Logger.logf("[#] TIEMPO PROMEDIO DE ENRUTAMIENTO POR PRODUCTO: %.3f us.%n",get_mean_time_by_prod());
            }

            public static void next_lot(int cantProd) {
                posProd += cantProd;
                numProd += cantProd;
            }

            public static void next_ped() {
                posPed++;
                numPed++;
            }
        }
    }
}

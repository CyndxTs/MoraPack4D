/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SeedRunner.java
 [**/

package com.pucp.dp1.grupo4d.morapack;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Reinicia la base de datos:
 *  1. Elimina TODAS las tablas de la BD actual (incluidas las de esquemas anteriores).
 *  2. Ejecuta en orden alfabético los scripts classpath:db/seed/*.sql (01_ddl.sql, luego los DML que se agreguen).
 * Solo se activa con el argumento --seed (ver MorapackApplication) y termina la aplicación al acabar.
 */
@Component
@ConditionalOnProperty(name = "morapack.seed.enabled", havingValue = "true")
public class SeedRunner implements ApplicationRunner {
    private static final String PATRON_SCRIPTS = "classpath:db/seed/*.sql";

    private final DataSource dataSource;
    private final ConfigurableApplicationContext context;

    public SeedRunner(DataSource dataSource, ConfigurableApplicationContext context) {
        this.dataSource = dataSource;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        int codigoSalida = 0;
        try {
            System.out.println(">> SEED: reiniciando base de datos..");
            this.eliminarTablas();
            Resource[] scripts = new PathMatchingResourcePatternResolver().getResources(PATRON_SCRIPTS);
            Arrays.sort(scripts, Comparator.comparing(Resource::getFilename));
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setSqlScriptEncoding("UTF-8");
            populator.setContinueOnError(false);
            for (Resource script : scripts) {
                System.out.printf("[:] SEED: script '%s'%n", script.getFilename());
                populator.addScript(script);
            }
            populator.execute(dataSource);
            System.out.printf("[~] SEED COMPLETADO! ('%d' scripts)%n", scripts.length);
        } catch (Exception e) {
            codigoSalida = 1;
            System.out.printf("[*] SEED FALLIDO: %s%n", e.getMessage());
            e.printStackTrace();
        }
        final int salida = codigoSalida;
        System.exit(SpringApplication.exit(context, () -> salida));
    }

    private void eliminarTablas() throws Exception {
        try (Connection conexion = dataSource.getConnection(); Statement st = conexion.createStatement()) {
            List<String> tablas = new ArrayList<>();
            try (ResultSet rs = st.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'")) {
                while (rs.next()) tablas.add(rs.getString(1));
            }
            System.out.printf("[:] SEED: base de datos '%s' | eliminando '%d' tablas..%n", conexion.getCatalog(), tablas.size());
            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String tabla : tablas) {
                st.execute("DROP TABLE IF EXISTS `" + tabla + "`");
            }
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}

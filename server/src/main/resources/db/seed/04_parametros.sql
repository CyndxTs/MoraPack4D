-- =====================================================================================
--  MoraPack4D - DML: parámetros iniciales (fila única, id = 1)
--  - Las sedes NO van aquí: son AEROPUERTO.es_sede. Al importar el archivo de aeropuertos se marcan
--    SPIM, EBCI y UBBB (si aún no hay sedes). Luego el administrador puede cambiarlas con PARAMETROS.
-- =====================================================================================

INSERT INTO `PARAMETROS` (
  `id`,
  `max_dias_entrega_intracontinental`, `max_dias_entrega_intercontinental`,
  `max_horas_recojo`, `max_horas_estancia`, `min_horas_estancia`,
  `prob_replanificacion`,
  `d_min`, `i_max`, `ele_min`, `ele_max`, `k_min`, `k_max`, `n_max`, `t_max`,
  `f_ua`, `f_ut`, `f_de`, `f_do`, `f_lj`,
  `replanificacion_automatica`, `minutos_espera_replanificacion`, `replanificar_al_registrar_evento`,
  `segundos_umbral_replanificacion`, `purga_automatica`
) VALUES (
  1,
  2, 3,
  2.0, 12.0, 1.0,
  0.350,
  0.005, 3, 1, 2, 3, 5, 6, 7,
  1.015, 5000.0, 2000.0, 3000.0, 0.0085,
  TRUE, 15, TRUE,
  180, TRUE
);

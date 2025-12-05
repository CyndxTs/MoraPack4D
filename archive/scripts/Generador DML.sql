INSERT INTO PARAMETROS (id,max_dias_entrega_intracontinental,max_dias_entrega_intercontinental,max_horas_recojo,max_horas_estancia,min_horas_estancia,probabilidad_replanificacion,d_min,i_max,ele_min,ele_max,k_min,k_max,n_max,t_max,f_ua,f_ut,f_de,f_do)
VALUES (1,2,3,2.0,12.0,1.0,1.1,0.005,3,1,2,3,5,6,7,1.015,5000.0,2000.0,3000.0)
ON DUPLICATE KEY UPDATE max_dias_entrega_intracontinental=VALUES(max_dias_entrega_intracontinental),
					    max_dias_entrega_intercontinental=VALUES(max_dias_entrega_intercontinental),
						max_horas_recojo=VALUES(max_horas_recojo), max_horas_estancia=VALUES(max_horas_estancia),
						min_horas_estancia=VALUES(min_horas_estancia),
						probabilidad_replanificacion=VALUES(probabilidad_replanificacion),
						d_min=VALUES(d_min),
						i_max=VALUES(i_max),
						ele_min=VALUES(ele_min),
						ele_max=VALUES(ele_max),
						k_min=VALUES(k_min),
						k_max=VALUES(k_max),
						n_max=VALUES(n_max),
						t_max=VALUES(t_max),
						f_ua=VALUES(f_ua),
						f_ut=VALUES(f_ut),
						f_de=VALUES(f_de),
						f_do=VALUES(f_do);
						
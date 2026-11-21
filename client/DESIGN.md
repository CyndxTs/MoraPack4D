# MoraPack — sistema visual

**Mundo:** torre de control nocturna. Cielo azul profundo, rutas en luz celeste y la carga (aviones, arcos, plazos) en ámbar,
heredero del dorado original de la marca. Tema oscuro en todo el sistema.

## Tokens (`src/styles/_variables.scss`)
| Token | Valor | Uso |
|---|---|---|
| `--abismo` | `#060B1A` | fondo de página |
| `--marino` / `-2` / `-3` | `#0C1631` / `#12204A` / `#182A5C` | superficies, hover, seleccionado |
| `--trazo` | `#22356A` | bordes y divisores |
| `--tinta` / `-2` / `-3` | `#EAF0FF` / `#A7B6DA` / `#7487B3` | texto principal, secundario, terciario |
| `--cielo` | `#78C6FF` | acción principal, foco, rutas, sedes |
| `--ambar` | `#FFB547` | carga, vuelos, señal (pestaña activa) |
| `--ok` / `--aviso` / `--alerta` | `#39D98A` / `#FFC24D` / `#FF6B7A` | estados |

Los nombres antiguos (`--color-gold`, `--color-dark-blue`, …) apuntan a estos tokens.

## Tipografía
Archivo (Google Fonts, eje de ancho). Titulares expandidos (`wdth` 112–125, peso 750–800, como letreros de aeropuerto);
texto a ancho normal. Cifras tabulares en tablas y tableros.

## Piezas
- **Logo** (`components/Brand/Logo.jsx`): caja isométrica ámbar con una órbita celeste que la rodea y termina en flecha.
- **Globo 3D** (`components/Globo/Globo.jsx`, three.js): tierra en puntos, sedes en celeste, vuelos ámbar con un pulso.
  Portada y fondo del modal de acceso. Se pausa fuera de pantalla; quieto con "reducir movimiento".
- **Mapa 2D** (`scenario/Basemap.jsx`): países de Natural Earth 1:50m (`public/geo/paises.json`, generado con nombres en
  español y punto de etiqueta) sobre océano azul noche; los nombres aparecen según el zoom. Sin mosaicos.
- **Botones**: `.boton--primario` (celeste, texto oscuro), `.boton--fantasma` (borde). Píldoras.

## Movimiento
Un solo momento por pantalla: entrada del hero + globo en la portada. Interacciones con `cubic-bezier(0.23, 1, 0.32, 1)`,
150–220 ms. Nada aparece al hacer scroll.

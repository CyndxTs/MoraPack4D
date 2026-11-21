import React, { useEffect, useRef } from "react";
import * as THREE from "three";
import { AEROPUERTOS, SEDES } from "../../data/redMoraPack";
import "./globo.scss";

// Globo 3D de la portada: tierra en puntos, las 3 sedes en celeste y vuelos (arcos) en ámbar con un pulso que viaja.
// Se pausa fuera de pantalla o con la pestaña oculta; con "reducir movimiento" queda quieto.

const RAD = Math.PI / 180;
const CIELO = new THREE.Color("#78C6FF");
const AMBAR = new THREE.Color("#FFB547");

function aVector(lat, lon, r = 1) {
  const phi = (90 - lat) * RAD;
  const theta = (lon + 180) * RAD;
  return new THREE.Vector3(-r * Math.sin(phi) * Math.cos(theta), r * Math.cos(phi), r * Math.sin(phi) * Math.sin(theta));
}

// Arco de círculo máximo elevado según la distancia
function curvaDeVuelo(a, b) {
  const va = aVector(a.lat, a.lon).normalize();
  const vb = aVector(b.lat, b.lon).normalize();
  const angulo = va.angleTo(vb);
  const altura = 0.06 + 0.28 * (angulo / Math.PI);
  const total = new THREE.Quaternion().setFromUnitVectors(va, vb);
  const identidad = new THREE.Quaternion();
  const puntos = [];
  const n = 64;
  for (let i = 0; i <= n; i++) {
    const t = i / n;
    const q = new THREE.Quaternion().slerpQuaternions(identidad, total, t);
    puntos.push(va.clone().applyQuaternion(q).multiplyScalar(1.002 + altura * Math.sin(Math.PI * t)));
  }
  return new THREE.CatmullRomCurve3(puntos);
}

const VERT_PUNTOS = /* glsl */ `
  uniform float uTam;
  varying float vFrente;
  void main() {
    vec4 mv = modelViewMatrix * vec4(position, 1.0);
    vec3 n = normalize(normalMatrix * normalize(position));
    vFrente = dot(n, normalize(-mv.xyz));
    gl_PointSize = uTam * (1.0 / -mv.z);
    gl_Position = projectionMatrix * mv;
  }`;
const FRAG_PUNTOS = /* glsl */ `
  uniform vec3 uColor;
  varying float vFrente;
  void main() {
    vec2 c = gl_PointCoord - 0.5;
    float d = length(c);
    if (d > 0.5) discard;
    float borde = smoothstep(0.5, 0.25, d);
    float luz = smoothstep(-0.05, 0.55, vFrente);
    gl_FragColor = vec4(uColor, borde * mix(0.05, 0.95, luz));
  }`;

const VERT_ARCO = /* glsl */ `
  varying float vU;
  void main() { vU = uv.x; gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0); }`;
const FRAG_ARCO = /* glsl */ `
  uniform float uT;
  uniform float uDesfase;
  uniform vec3 uColor;
  varying float vU;
  void main() {
    float cabeza = fract(uT * 0.22 + uDesfase);
    float d = cabeza - vU;
    float estela = d >= 0.0 ? exp(-d * 9.0) : 0.0;
    float base = 0.16;
    float a = clamp(base + estela, 0.0, 1.0);
    gl_FragColor = vec4(uColor * (0.8 + estela * 0.6), a);
  }`;

const VERT_ATM = /* glsl */ `
  varying vec3 vN; varying vec3 vV;
  void main() { vec4 mv = modelViewMatrix * vec4(position, 1.0); vN = normalize(normalMatrix * normal); vV = normalize(-mv.xyz); gl_Position = projectionMatrix * mv; }`;
const FRAG_ATM = /* glsl */ `
  varying vec3 vN; varying vec3 vV;
  void main() { float f = pow(1.0 - abs(dot(vN, vV)), 3.0); gl_FragColor = vec4(0.35, 0.62, 1.0, f * 0.5); }`;

export default function Globo({ className = "", girar = true, opacidad = 1 }) {
  const contenedor = useRef(null);

  useEffect(() => {
    const el = contenedor.current;
    if (!el) return undefined;
    let renderer;
    try {
      renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, powerPreference: "low-power" });
    } catch {
      el.classList.add("globo--sin-webgl");
      return undefined;
    }
    const reducir = window.matchMedia?.("(prefers-reduced-motion: reduce)").matches;
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    el.appendChild(renderer.domElement);

    const escena = new THREE.Scene();
    const camara = new THREE.PerspectiveCamera(32, 1, 0.1, 100);
    camara.position.set(0, 0, 4.2);

    const mundo = new THREE.Group();
    mundo.rotation.set(0.38, 0, 0);
    escena.add(mundo);
    const giro = new THREE.Group();
    mundo.add(giro);

    // Esfera base y atmósfera
    giro.add(new THREE.Mesh(new THREE.SphereGeometry(0.995, 64, 64), new THREE.MeshBasicMaterial({ color: "#0A1534" })));
    const atm = new THREE.Mesh(
      new THREE.SphereGeometry(1.1, 64, 64),
      new THREE.ShaderMaterial({ vertexShader: VERT_ATM, fragmentShader: FRAG_ATM, side: THREE.BackSide, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending }),
    );
    mundo.add(atm);

    const recursos = [];
    const uniformsPuntos = { uTam: { value: 13 * renderer.getPixelRatio() }, uColor: { value: new THREE.Color("#7FA3F5") } };
    const matPuntos = new THREE.ShaderMaterial({ vertexShader: VERT_PUNTOS, fragmentShader: FRAG_PUNTOS, uniforms: uniformsPuntos, transparent: true, depthWrite: false });
    recursos.push(matPuntos);

    let vivo = true;
    fetch("/geo/tierra.json").then((r) => r.json()).then((arr) => {
      if (!vivo) return;
      const pos = new Float32Array((arr.length / 2) * 3);
      for (let i = 0, k = 0; i < arr.length; i += 2, k += 3) {
        const v = aVector(arr[i], arr[i + 1], 1.0);
        pos[k] = v.x; pos[k + 1] = v.y; pos[k + 2] = v.z;
      }
      const g = new THREE.BufferGeometry();
      g.setAttribute("position", new THREE.BufferAttribute(pos, 3));
      recursos.push(g);
      giro.add(new THREE.Points(g, matPuntos));
      if (reducir) dibujar(0);
    }).catch(() => {});

    // Vuelos: sede -> cada destino, y entre sedes
    const arcos = [];
    const rutas = [];
    AEROPUERTOS.filter((a) => !a.sede).forEach((a, i) => {
      const sede = a.continente === "america" ? SEDES[0] : a.continente === "europa" ? SEDES[1] : SEDES[2];
      const otra = SEDES[(SEDES.indexOf(sede) + 1 + (i % 2)) % 3];
      rutas.push([sede, a]);
      if (i % 4 === 0) rutas.push([otra, a]);
    });
    rutas.push([SEDES[0], SEDES[1]], [SEDES[1], SEDES[2]], [SEDES[2], SEDES[0]]);
    rutas.forEach(([a, b], i) => {
      const geo = new THREE.TubeGeometry(curvaDeVuelo(a, b), 64, 0.0032, 4, false);
      const mat = new THREE.ShaderMaterial({
        vertexShader: VERT_ARCO, fragmentShader: FRAG_ARCO, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending,
        uniforms: { uT: { value: 0 }, uDesfase: { value: (i * 0.137) % 1 }, uColor: { value: (a.sede && b.sede) ? CIELO : AMBAR } },
      });
      recursos.push(geo, mat);
      arcos.push(mat);
      giro.add(new THREE.Mesh(geo, mat));
    });

    // Aeropuertos y sedes
    const geoPunto = new THREE.SphereGeometry(1, 12, 12);
    recursos.push(geoPunto);
    const anillos = [];
    AEROPUERTOS.forEach((a) => {
      const m = new THREE.Mesh(geoPunto, new THREE.MeshBasicMaterial({ color: a.sede ? CIELO : AMBAR }));
      m.scale.setScalar(a.sede ? 0.016 : 0.0075);
      m.position.copy(aVector(a.lat, a.lon, 1.003));
      giro.add(m);
      recursos.push(m.material);
      if (a.sede) {
        const anillo = new THREE.Mesh(new THREE.RingGeometry(0.02, 0.026, 40), new THREE.MeshBasicMaterial({ color: CIELO, transparent: true, side: THREE.DoubleSide, depthWrite: false }));
        anillo.position.copy(aVector(a.lat, a.lon, 1.004));
        anillo.lookAt(new THREE.Vector3(0, 0, 0));
        giro.add(anillo);
        recursos.push(anillo.geometry, anillo.material);
        anillos.push(anillo);
      }
    });

    // Vista inicial: Atlántico (América y Europa a la vista)
    giro.rotation.y = -0.55;

    // Arrastre con inercia
    let arrastrando = false, ultimoX = 0, velocidad = 0;
    const onDown = (e) => { arrastrando = true; ultimoX = e.clientX; el.setPointerCapture?.(e.pointerId); };
    const onMove = (e) => { if (!arrastrando) return; const dx = e.clientX - ultimoX; ultimoX = e.clientX; giro.rotation.y += dx * 0.005; velocidad = dx * 0.005; };
    const onUp = () => { arrastrando = false; };
    el.addEventListener("pointerdown", onDown);
    window.addEventListener("pointermove", onMove);
    window.addEventListener("pointerup", onUp);

    const ajustar = () => {
      const w = el.clientWidth || 1, h = el.clientHeight || 1;
      renderer.setSize(w, h, false);
      camara.aspect = w / h;
      camara.updateProjectionMatrix();
    };
    const ro = new ResizeObserver(ajustar);
    ro.observe(el);
    ajustar();

    function dibujar(t) {
      arcos.forEach((m) => (m.uniforms.uT.value = t));
      anillos.forEach((r, i) => { const k = ((t * 0.6 + i * 0.33) % 1); r.scale.setScalar(1 + k * 1.6); r.material.opacity = 0.8 * (1 - k); });
      renderer.render(escena, camara);
    }

    let raf = 0, visible = true, previo = performance.now(), tiempo = 0;
    const io = new IntersectionObserver(([e]) => { visible = e.isIntersecting; });
    io.observe(el);
    const bucle = (ahora) => {
      raf = requestAnimationFrame(bucle);
      const dt = Math.min(0.05, (ahora - previo) / 1000);
      previo = ahora;
      if (!visible || document.hidden) return;
      tiempo += dt;
      if (!arrastrando) {
        velocidad *= 0.94;
        giro.rotation.y += (girar ? 0.0011 : 0) + velocidad;
      }
      dibujar(tiempo);
    };
    if (reducir) dibujar(1.3);
    else raf = requestAnimationFrame(bucle);

    return () => {
      vivo = false;
      cancelAnimationFrame(raf);
      ro.disconnect();
      io.disconnect();
      el.removeEventListener("pointerdown", onDown);
      window.removeEventListener("pointermove", onMove);
      window.removeEventListener("pointerup", onUp);
      recursos.forEach((r) => r.dispose?.());
      atm.geometry.dispose(); atm.material.dispose();
      renderer.dispose();
      renderer.domElement.remove();
    };
  }, [girar]);

  return <div ref={contenedor} className={`globo ${className}`} style={{ opacity: opacidad }} aria-hidden="true" />;
}

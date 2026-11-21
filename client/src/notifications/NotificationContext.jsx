import React, { createContext, useCallback, useContext, useMemo, useRef, useState } from "react";
import { Notification } from "../components/UI/ui";
import "./notifications.scss";

// Notificaciones globales (antes cada página tenía su propio estado y se pisaban entre sí)
const NotificationContext = createContext({ notify: () => {} });

// Éxito: se va solo y rápido. Error/aviso: se cierra con la X o solo a los 3 s.
const DURACION_MS = { success: 1200, info: 2000, warning: 3000, danger: 3000 };
const MAXIMO = 3;

export function NotificationProvider({ children }) {
  const [items, setItems] = useState([]);
  const seq = useRef(0);

  const itemsRef = useRef([]);
  const cerrar = useCallback((id) => {
    itemsRef.current = itemsRef.current.filter((n) => n.id !== id);
    setItems(itemsRef.current);
  }, []);

  const timers = useRef(new Map());

  const programarCierre = useCallback((id, type) => {
    clearTimeout(timers.current.get(id));
    timers.current.set(id, setTimeout(() => { timers.current.delete(id); cerrar(id); }, DURACION_MS[type] ?? 2000));
  }, [cerrar]);

  const notify = useCallback((type, message) => {
    // El mismo aviso repetido no se apila: se reinicia su tiempo
    const igual = itemsRef.current.find((n) => n.type === type && n.message === message);
    if (igual) { programarCierre(igual.id, type); return; }
    const id = ++seq.current;
    itemsRef.current = [...itemsRef.current.slice(-(MAXIMO - 1)), { id, type, message }];
    setItems(itemsRef.current);
    programarCierre(id, type);
  }, [programarCierre]);

  const value = useMemo(() => ({ notify }), [notify]);

  return (
    <NotificationContext.Provider value={value}>
      {children}
      <div className="notification-stack">
        {items.map((n) => (
          <Notification key={n.id} type={n.type} message={n.message} onClose={() => { clearTimeout(timers.current.get(n.id)); cerrar(n.id); }} />
        ))}
      </div>
    </NotificationContext.Provider>
  );
}

export const useNotify = () => useContext(NotificationContext).notify;

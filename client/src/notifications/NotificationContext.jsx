import React, { createContext, useCallback, useContext, useMemo, useRef, useState } from "react";
import { Notification } from "../components/UI/ui";
import "./notifications.scss";

// Notificaciones globales (antes cada página tenía su propio estado y se pisaban entre sí)
const NotificationContext = createContext({ notify: () => {} });

const DURACION_MS = 5000;
const MAXIMO = 4;

export function NotificationProvider({ children }) {
  const [items, setItems] = useState([]);
  const seq = useRef(0);

  const cerrar = useCallback((id) => setItems((prev) => prev.filter((n) => n.id !== id)), []);

  const notify = useCallback((type, message) => {
    const id = ++seq.current;
    setItems((prev) => [...prev.slice(-(MAXIMO - 1)), { id, type, message }]);
    setTimeout(() => cerrar(id), DURACION_MS);
  }, [cerrar]);

  const value = useMemo(() => ({ notify }), [notify]);

  return (
    <NotificationContext.Provider value={value}>
      {children}
      <div className="notification-stack">
        {items.map((n) => (
          <Notification key={n.id} type={n.type} message={n.message} onClose={() => cerrar(n.id)} />
        ))}
      </div>
    </NotificationContext.Provider>
  );
}

export const useNotify = () => useContext(NotificationContext).notify;

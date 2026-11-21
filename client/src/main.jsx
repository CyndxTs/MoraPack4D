import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import router from "./app/router";
import { AuthProvider } from "./auth/AuthContext";
import { NotificationProvider } from "./notifications/NotificationContext";
import { ImportacionesProvider } from "./importaciones/ImportacionesContext";
import "./styles/main.scss";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <NotificationProvider>
      <AuthProvider>
        <ImportacionesProvider>
          <RouterProvider router={router} />
        </ImportacionesProvider>
      </AuthProvider>
    </NotificationProvider>
  </React.StrictMode>
);

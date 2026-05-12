import React from "react";
import { createRoot } from "react-dom/client";
import App from "./App";
import "./index.css";
import { apiClient } from "@/lib/api-client";

// Expose React, the shared apiClient, and a settings accessor globally so
// plugin IIFE bundles can use the same instances and preferences.
// Must happen before any plugin bundle is injected.
(window as any).React = React;
(window as any).__elementsApiClient = apiClient;
(window as any).__elementsSettings = {
  getResultsPerPage(): number {
    return parseInt(localStorage.getItem('admin-results-per-page') ?? '20', 10);
  },
};
(window as any).__elementsPlugins = {
  _registry: {} as Record<string, React.ComponentType>,
  register(route: string, component: React.ComponentType) {
    (window as any).__elementsPlugins._registry[route] = component;
  },
};

createRoot(document.getElementById("root")!).render(<App />);

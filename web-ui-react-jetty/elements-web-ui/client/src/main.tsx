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
// Registrations are namespaced by the loader's active namespace (set right
// before each plugin bundle is injected) so that plugins from different
// deployments can reuse the same `route` value without colliding.
const pluginRegistry: Record<string, Record<string, React.ComponentType>> = {};
let activeNamespace: string | null = null;
(window as any).__elementsPlugins = {
  get _registry() {
    return pluginRegistry;
  },
  get _activeNamespace() {
    return activeNamespace;
  },
  set _activeNamespace(value: string | null) {
    activeNamespace = value;
  },
  register(route: string, component: React.ComponentType) {
    const bucket: Record<string, React.ComponentType> =
      (pluginRegistry[activeNamespace ?? ''] ??= {});
    if (bucket[route]) {
      console.warn(
        `[Elements] Plugin route collision: "${route}" is already registered${activeNamespace ? ` under namespace "${activeNamespace}"` : ''}. The last registration wins.`
      );
    }
    bucket[route] = component;
  },
};

createRoot(document.getElementById("root")!).render(<App />);

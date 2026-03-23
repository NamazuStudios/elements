import React from "react";
import { createRoot } from "react-dom/client";
import App from "./App";
import "./index.css";

// Expose React globally so plugin IIFE bundles can use the same instance.
// Must happen before any plugin bundle is injected.
(window as any).React = React;
(window as any).__elementsPlugins = {
  _registry: {} as Record<string, React.ComponentType>,
  register(route: string, component: React.ComponentType) {
    (window as any).__elementsPlugins._registry[route] = component;
  },
};

createRoot(document.getElementById("root")!).render(<App />);

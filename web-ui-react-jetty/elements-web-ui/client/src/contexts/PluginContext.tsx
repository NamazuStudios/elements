import React, { createContext, useContext, useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from './AuthContext';
import { discoverAndLoadPlugins, LoadedPlugin } from '@/lib/plugin-loader';

interface PluginContextValue {
  plugins: LoadedPlugin[];
  isLoading: boolean;
}

const PluginContext = createContext<PluginContextValue>({ plugins: [], isLoading: false });

export function PluginProvider({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  const [plugins, setPlugins] = useState<LoadedPlugin[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const { data: containers } = useQuery<Array<{ uris?: string[] }>>({
    queryKey: ['/api/rest/elements/container'],
    enabled: isAuthenticated,
  });

  useEffect(() => {
    if (!isAuthenticated || !containers) return;

    setIsLoading(true);
    discoverAndLoadPlugins(containers)
      .then(loaded => setPlugins(loaded))
      .catch(() => setPlugins([]))
      .finally(() => setIsLoading(false));
  }, [isAuthenticated, containers]);

  return (
    <PluginContext.Provider value={{ plugins, isLoading }}>
      {children}
    </PluginContext.Provider>
  );
}

export function usePlugins() {
  return useContext(PluginContext);
}

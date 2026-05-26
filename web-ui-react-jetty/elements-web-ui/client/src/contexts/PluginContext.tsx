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
  const { isAuthenticated, userLevel } = useAuth();
  const [plugins, setPlugins] = useState<LoadedPlugin[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // Derive segment from userLevel; defaults to 'superuser' until user-level dashboards are introduced.
  const segment = userLevel?.toLowerCase() ?? 'superuser';

  const { data: containers } = useQuery<Array<{ uris?: string[]; runtime?: { deployment?: { application?: { name?: string } } } }>>({
    queryKey: ['/api/rest/elements/container'],
    enabled: isAuthenticated,
  });

  useEffect(() => {
    if (!isAuthenticated || !containers) return;

    const mappedContainers = containers.map(c => ({
      uris: c.uris,
      application: c.runtime?.deployment?.application?.name,
    }));

    setIsLoading(true);
    discoverAndLoadPlugins(mappedContainers, segment)
      .then(loaded => setPlugins(loaded))
      .catch(() => setPlugins([]))
      .finally(() => setIsLoading(false));
  }, [isAuthenticated, containers, segment]);

  return (
    <PluginContext.Provider value={{ plugins, isLoading }}>
      {children}
    </PluginContext.Provider>
  );
}

export function usePlugins() {
  return useContext(PluginContext);
}

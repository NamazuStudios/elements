import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { discoverResources, ResourceDefinition } from '@/lib/resource-discovery';
import { useAuth } from './AuthContext';

interface ResourceContextType {
  resources: ResourceDefinition[];
  isLoading: boolean;
  isInitialized: boolean;
  refetch: () => Promise<void>;
}

const ResourceContext = createContext<ResourceContextType | undefined>(undefined);

export function ResourceProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  const [resources, setResources] = useState<ResourceDefinition[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);

  const fetchResources = async () => {
    if (!isAuthenticated) {
      setResources([]);
      setIsInitialized(true);
      return;
    }

    setIsLoading(true);
    try {
      const discovered = await discoverResources();
      setResources(discovered);
    } catch (error) {
      console.error('Failed to discover resources:', error);
      setResources([]);
    } finally {
      setIsLoading(false);
      setIsInitialized(true);
    }
  };

  useEffect(() => {
    fetchResources();
  }, [isAuthenticated]);

  return (
    <ResourceContext.Provider value={{ resources, isLoading, isInitialized, refetch: fetchResources }}>
      {children}
    </ResourceContext.Provider>
  );
}

export function useResources() {
  const context = useContext(ResourceContext);
  if (context === undefined) {
    throw new Error('useResources must be used within a ResourceProvider');
  }
  return context;
}

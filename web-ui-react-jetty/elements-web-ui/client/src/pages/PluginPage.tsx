import { useParams } from 'wouter';
import { usePlugins } from '@/contexts/PluginContext';
import { PluginErrorBoundary } from '@/components/PluginErrorBoundary';

export default function PluginPage() {
  const params = useParams<{ route: string }>();
  const { plugins, isLoading } = usePlugins();
  const route = params.route;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[200px]">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-muted-foreground">Loading plugin...</p>
        </div>
      </div>
    );
  }

  const plugin = plugins.find(p => p.route === route);

  if (!plugin) {
    return (
      <div className="p-6">
        <h2 className="text-lg font-semibold mb-2">Plugin Not Found</h2>
        <p className="text-muted-foreground">No plugin registered for route: <code>{route}</code></p>
      </div>
    );
  }

  const PluginComponent = plugin.component;

  return (
    <PluginErrorBoundary pluginName={plugin.label}>
      <PluginComponent />
    </PluginErrorBoundary>
  );
}

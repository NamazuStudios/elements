import { Router, Switch, Route, Redirect, useParams } from "wouter";
import { queryClient } from "./lib/queryClient";
import { QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { AuthProvider, useAuth } from "@/contexts/AuthContext";
import { ThemeProvider } from "@/components/ThemeProvider";
import { ResourceProvider, useResources } from "@/contexts/ResourceContext";
import LoginPage from "@/components/LoginPage";
import DashboardLayout from "@/components/DashboardLayout";
import Dashboard from "@/pages/Dashboard";
import ResourceManager from "@/pages/ResourceManager";
import LargeObjects from "@/pages/LargeObjects";
import MultiMatch from "@/pages/MultiMatch";
import Vaults from "@/pages/Vaults";
import Settings from "@/pages/Settings";
import InstalledElements from "@/pages/InstalledElements";
import DynamicApiExplorer from "@/pages/DynamicApiExplorer";
import ElementApiExplorer from "@/pages/ElementApiExplorer";
import CoreElements from "@/pages/CoreElements";
import NotFound from "@/pages/not-found";

function ResourceRoute() {
  const params = useParams<{ resourceId: string }>();
  const { resources, isLoading, isInitialized } = useResources();
  const resourceId = params.resourceId?.toLowerCase();
  
  if (isLoading || !isInitialized) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-muted-foreground">Loading resources...</p>
        </div>
      </div>
    );
  }
  
  const resource = resources.find(r => r.name.toLowerCase().replace(/\s+/g, '-') === resourceId);
  
  if (!resource) {
    return <Redirect to="/dashboard" />;
  }
  
  return (
    <ProtectedRoute 
      component={() => (
        <ResourceManager 
          resourceName={resource.name} 
          endpoint={resource.endpoint} 
        />
      )} 
    />
  );
}

function ProtectedRoute({ component: Component }: { component: React.ComponentType }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-muted-foreground">Loading...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Redirect to="/login" />;
  }

  return (
    <DashboardLayout>
      <Component />
    </DashboardLayout>
  );
}

function Routes() {
  const { isAuthenticated } = useAuth();

  return (
    <Switch>
      <Route path="/login">
        {isAuthenticated ? <Redirect to="/dashboard" /> : <LoginPage />}
      </Route>
      <Route path="/">
        {isAuthenticated ? <Redirect to="/dashboard" /> : <Redirect to="/login" />}
      </Route>
      <Route path="/dashboard">
        <ProtectedRoute component={Dashboard} />
      </Route>
      <Route path="/settings">
        <ProtectedRoute component={Settings} />
      </Route>
      <Route path="/installed-elements">
        <ProtectedRoute component={InstalledElements} />
      </Route>
      <Route path="/dynamic-api-explorer">
        <ProtectedRoute component={DynamicApiExplorer} />
      </Route>
      <Route path="/core-elements">
        <ProtectedRoute component={CoreElements} />
      </Route>
      <Route path="/element-api-explorer">
        <ProtectedRoute component={ElementApiExplorer} />
      </Route>
      <Route path="/resource/large-objects">
        <ProtectedRoute component={LargeObjects} />
      </Route>
      <Route path="/resource/matchmaking">
        <ProtectedRoute component={MultiMatch} />
      </Route>
      <Route path="/resource/vaults">
        <ProtectedRoute component={Vaults} />
      </Route>
      <Route path="/resource/:resourceId">
        <ResourceRoute />
      </Route>
      <Route component={NotFound} />
    </Switch>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ResourceProvider>
          <ThemeProvider defaultTheme="dark" storageKey="elements-admin-theme">
            <TooltipProvider>
              <Router base="/admin">
                <Toaster />
                <Routes />
              </Router>
            </TooltipProvider>
          </ThemeProvider>
        </ResourceProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;

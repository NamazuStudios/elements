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
    <ResourceManager 
      resourceName={resource.name} 
      endpoint={resource.endpoint} 
    />
  );
}

function Routes() {
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
    return (
      <Switch>
        <Route path="/login">
          <LoginPage />
        </Route>
        <Route>
          <Redirect to="/login" />
        </Route>
      </Switch>
    );
  }

  return (
    <DashboardLayout>
      <Switch>
        <Route path="/login">
          <Redirect to="/dashboard" />
        </Route>
        <Route path="/">
          <Redirect to="/dashboard" />
        </Route>
        <Route path="/dashboard">
          <Dashboard />
        </Route>
        <Route path="/settings">
          <Settings />
        </Route>
        <Route path="/installed-elements">
          <InstalledElements />
        </Route>
        <Route path="/dynamic-api-explorer">
          <DynamicApiExplorer />
        </Route>
        <Route path="/core-elements">
          <CoreElements />
        </Route>
        <Route path="/element-api-explorer">
          <ElementApiExplorer />
        </Route>
        <Route path="/resource/large-objects">
          <LargeObjects />
        </Route>
        <Route path="/resource/matchmaking">
          <MultiMatch />
        </Route>
        <Route path="/resource/vaults">
          <Vaults />
        </Route>
        <Route path="/resource/:resourceId">
          <ResourceRoute />
        </Route>
        <Route component={NotFound} />
      </Switch>
    </DashboardLayout>
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

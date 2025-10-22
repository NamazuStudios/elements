import { useQuery } from '@tanstack/react-query';
import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubItem,
  SidebarMenuSubButton,
} from '@/components/ui/sidebar';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import * as Icons from 'lucide-react';

interface ApplicationElement {
  type: string;
  definition: {
    name: string;
    recursive: boolean;
    additionalPackages: Array<any>;
    loader: string;
  };
  services: Array<any>;
  producedEvents: Array<any>;
  consumedEvents: Array<any>;
  dependencies: Array<any>;
  attributes: Record<string, any>;
  defaultAttributes: Array<any>;
}

interface ApplicationStatus {
  application: {
    id: string;
    name: string;
    description?: string;
  };
  status: 'CLEAN' | 'FAILED' | string;
  uris: string[];
  logs: string[];
  elements: ApplicationElement[];
}

interface InstalledElementsSidebarProps {
  location: string;
  setLocation: (path: string) => void;
}

export function InstalledElementsSidebar({ location, setLocation }: InstalledElementsSidebarProps) {
  // Fetch backend URL from config
  const { data: config } = useQuery<{ apiUrl: string }>({
    queryKey: ['/api/config'],
    staleTime: Infinity, // Config shouldn't change during runtime
  });
  
  // Fetch all applications with installed elements from the Elements backend
  const { data: applicationStatuses } = useQuery<ApplicationStatus[]>({
    queryKey: ['/api/proxy/api/rest/elements/application'],
    enabled: true,
    staleTime: 30000, // Cache for 30 seconds
  });

  // Fix element URI by replacing localhost with actual backend URL
  const fixElementUri = (uri: string | undefined): string => {
    if (!uri) return '';
    
    // Use backend URL from config, fallback to localhost for development
    const backendUrl = config?.apiUrl || 'http://localhost:8080';
    return uri.replace('http://localhost:8080', backendUrl);
  };

  // Filter applications with non-empty elements list (any status)
  const appsWithElements = applicationStatuses?.filter(
    appStatus => appStatus.elements && appStatus.elements.length > 0
  ) || [];

  // Always show the API Explorer section (at minimum, Core API will be shown)

  return (
    <Collapsible defaultOpen className="group/collapsible">
      <SidebarGroup>
        <SidebarGroupLabel asChild>
          <CollapsibleTrigger className="text-xs uppercase tracking-wider hover-elevate">
            API Explorer
            <Icons.ChevronDown className="ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180" />
          </CollapsibleTrigger>
        </SidebarGroupLabel>
        <CollapsibleContent>
          <SidebarGroupContent>
            <SidebarMenu>
              {/* Core API Explorer - always shown first */}
              <SidebarMenuItem>
                <SidebarMenuButton
                  onClick={() => setLocation('/dynamic-api-explorer')}
                  isActive={location === '/dynamic-api-explorer'}
                  data-testid="link-core-api-explorer"
                >
                  <Icons.Database className="w-4 h-4" />
                  <span>Core API</span>
                </SidebarMenuButton>
              </SidebarMenuItem>

              {/* Installed Elements - each element gets its own API explorer */}
              {appsWithElements.map((appStatus) => {
                // Determine status badge variant
                const statusVariant = appStatus.status === 'CLEAN' 
                  ? 'default' 
                  : appStatus.status === 'UNSTABLE' 
                    ? 'secondary' 
                    : 'destructive';
                
                return (
                  <Collapsible key={appStatus.application.id} defaultOpen className="group/app">
                    <SidebarMenuItem>
                      <CollapsibleTrigger asChild>
                        <SidebarMenuButton
                          className="w-full"
                          data-testid={`link-app-${appStatus.application.name?.toLowerCase().replace(/\s+/g, '-')}`}
                        >
                          <Icons.AppWindow className="w-4 h-4" />
                          <div className="flex flex-col items-start flex-1 min-w-0">
                            <span className="truncate">{appStatus.application.name || appStatus.application.id}</span>
                            <div className="flex items-center gap-1">
                              <div className={`w-1.5 h-1.5 rounded-full ${
                                appStatus.status === 'CLEAN' 
                                  ? 'bg-green-500' 
                                  : appStatus.status === 'UNSTABLE' 
                                    ? 'bg-yellow-500' 
                                    : 'bg-red-500'
                              }`} />
                              <span className="text-[10px] text-muted-foreground">{appStatus.status}</span>
                            </div>
                          </div>
                          <Icons.ChevronRight className="ml-auto transition-transform group-data-[state=open]/app:rotate-90" />
                        </SidebarMenuButton>
                      </CollapsibleTrigger>
                    <CollapsibleContent>
                      <SidebarMenuSub>
                        {appStatus.elements?.map((element, idx) => {
                          // Use serve.prefix as the display name, fallback to definition.name
                          const servePrefix = element.attributes?.['dev.getelements.elements.app.serve.prefix'];
                          const displayName = servePrefix || element.definition?.name?.split('.').pop() || `Element ${idx}`;
                          const elementIdentifier = element.definition?.name || `element-${idx}`;
                          
                          // Find URI for this element based on serve prefix
                          // Try HTTP/HTTPS first (for OpenAPI), then fall back to WebSocket URIs
                          let elementUri = servePrefix 
                            ? appStatus.uris.find(uri => uri.includes(`/${servePrefix}`) && (uri.startsWith('http://') || uri.startsWith('https://')))
                            : appStatus.uris.find(uri => uri.startsWith('http://') || uri.startsWith('https://'));
                          
                          // If no HTTP URI found, try WebSocket
                          if (!elementUri && servePrefix) {
                            elementUri = appStatus.uris.find(uri => uri.includes(`/${servePrefix}`) && (uri.startsWith('ws://') || uri.startsWith('wss://')));
                          }
                          
                          const fixedUri = fixElementUri(elementUri);
                          
                          // Determine icon based on element type or URI
                          const hasWebSocketUri = appStatus.uris.some(uri => uri.includes(servePrefix || '') && (uri.startsWith('ws://') || uri.startsWith('wss://')));
                          const IconComponent = hasWebSocketUri ? Icons.Wifi : Icons.Package;
                          
                          return (
                            <SidebarMenuSubItem key={`${appStatus.application.id}-${elementIdentifier}-${idx}`}>
                              <SidebarMenuSubButton
                                onClick={() => {
                                  setLocation(`/element-api-explorer?app=${encodeURIComponent(appStatus.application.id)}&element=${encodeURIComponent(elementIdentifier)}&uri=${encodeURIComponent(fixedUri || '')}`);
                                }}
                                isActive={
                                  location.includes('/element-api-explorer') &&
                                  location.includes(`app=${encodeURIComponent(appStatus.application.id)}`) &&
                                  location.includes(`element=${encodeURIComponent(elementIdentifier)}`)
                                }
                                data-testid={`link-element-${displayName.toLowerCase().replace(/\s+/g, '-')}`}
                              >
                                <IconComponent className="w-3 h-3" />
                                <span className="truncate">{displayName}</span>
                              </SidebarMenuSubButton>
                            </SidebarMenuSubItem>
                          );
                        })}
                      </SidebarMenuSub>
                    </CollapsibleContent>
                  </SidebarMenuItem>
                </Collapsible>
                );
              })}
            </SidebarMenu>
          </SidebarGroupContent>
        </CollapsibleContent>
      </SidebarGroup>
    </Collapsible>
  );
}

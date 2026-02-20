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
import { Badge } from '@/components/ui/badge';
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
  status: 'CLEAN' | 'UNSTABLE' | 'FAILED' | string;
  uris: string[];
  logs: string[];
  elements: ApplicationElement[];
}

interface InstalledElementsSidebarProps {
  location: string;
  setLocation: (path: string) => void;
}

export function InstalledElementsSidebar({ location, setLocation }: InstalledElementsSidebarProps) {
  // Fetch backend URL from config file
  const { data: config } = useQuery<{ api: { url: string } }>({
    queryKey: ['./config.json'],
    staleTime: Infinity, // Config shouldn't change during runtime
  });
  
  // Fetch all element containers (applications with installed elements) from the Elements backend
  const { data: applicationStatuses } = useQuery<ApplicationStatus[]>({
    queryKey: ['/api/rest/elements/container'],
    enabled: true,
    staleTime: 30000,
  });

  // Fix element URI by replacing localhost with actual backend URL
  const fixElementUri = (uri: string | undefined): string => {
    if (!uri) return '';
    
    // Use backend URL from config, fallback to localhost for development
    // Extract base URL from api.url (remove /api/rest suffix)
    const backendUrl = config?.api?.url 
      ? config.api.url.replace(/\/api\/rest\/?$/, '')
      : 'http://localhost:8080';
    return uri.replace('http://localhost:8080', backendUrl);
  };

  // Get status dot color based on application status
  const getStatusDotColor = (status: string) => {
    switch (status) {
      case 'CLEAN':
        return 'bg-green-500';
      case 'UNSTABLE':
        return 'bg-orange-500';
      case 'FAILED':
        return 'bg-red-500';
      default:
        return 'bg-gray-500';
    }
  };

  // All applications (display all, not just those with elements)
  const allApplications = applicationStatuses?.filter(
    appStatus => appStatus?.application
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

              {/* Core Elements - system elements and services */}
              <SidebarMenuItem>
                <SidebarMenuButton
                  onClick={() => setLocation('/core-elements')}
                  isActive={location === '/core-elements'}
                  data-testid="link-core-elements"
                >
                  <Icons.Box className="w-4 h-4" />
                  <span>Core Elements</span>
                </SidebarMenuButton>
              </SidebarMenuItem>

              {/* Element Deployments */}
              <SidebarMenuItem>
                <SidebarMenuButton
                  onClick={() => setLocation('/element-deployments')}
                  isActive={location === '/element-deployments'}
                  data-testid="link-element-deployments"
                >
                  <Icons.Rocket className="w-4 h-4" />
                  <span>Deployments</span>
                </SidebarMenuButton>
              </SidebarMenuItem>

              {/* Installed Elements - display ALL applications */}
              {allApplications.map((appStatus) => {
                const statusColor = getStatusDotColor(appStatus.status);
                const hasElements = appStatus.elements && appStatus.elements.length > 0;
                const hasLogs = appStatus.logs && appStatus.logs.length > 0;
                
                return (
                  <Collapsible 
                    key={appStatus.application.id} 
                    defaultOpen={hasElements} 
                    className="group/app"
                  >
                    <SidebarMenuItem>
                      <CollapsibleTrigger asChild>
                        <SidebarMenuButton
                          className="w-full"
                          data-testid={`link-app-${appStatus.application?.name?.toLowerCase().replace(/\s+/g, '-') || 'unknown'}`}
                        >
                          <Icons.AppWindow className="w-4 h-4" />
                          <div className="flex items-center gap-2 flex-1 min-w-0">
                            <span className="truncate flex-1">{appStatus.application.name || appStatus.application.id}</span>
                            <div className={`w-2 h-2 rounded-full flex-shrink-0 ${statusColor}`} title={appStatus.status} />
                          </div>
                          <Icons.ChevronRight className="ml-auto transition-transform group-data-[state=open]/app:rotate-90" />
                        </SidebarMenuButton>
                      </CollapsibleTrigger>
                    <CollapsibleContent>
                      <SidebarMenuSub>
                        {/* Show logs if status is not CLEAN and logs are available */}
                        {appStatus.status !== 'CLEAN' && hasLogs && (
                          <SidebarMenuSubItem>
                            <div className="px-2 py-1.5 text-[10px] text-muted-foreground">
                              <div className="font-medium mb-1">Logs:</div>
                              <div className="space-y-0.5 max-h-24 overflow-y-auto">
                                {appStatus.logs.slice(0, 5).map((log, idx) => (
                                  <div key={idx} className="truncate" title={log}>
                                    {log}
                                  </div>
                                ))}
                                {appStatus.logs.length > 5 && (
                                  <div className="text-muted-foreground/70">
                                    +{appStatus.logs.length - 5} more...
                                  </div>
                                )}
                              </div>
                            </div>
                          </SidebarMenuSubItem>
                        )}
                        
                        {/* Show elements if available */}
                        {hasElements && appStatus.elements.filter(element => element != null).map((element, idx) => {
                          // Use serve.prefix as the display name, fallback to definition.name
                          const servePrefix = element?.attributes?.['dev.getelements.elements.app.serve.prefix'];
                          const displayName = servePrefix || element?.definition?.name?.split('.').pop() || `Element ${idx}`;
                          const elementIdentifier = element?.definition?.name || `element-${idx}`;
                          
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
                        
                        {/* Show link to view application details if no elements */}
                        {!hasElements && (
                          <SidebarMenuSubItem>
                            <SidebarMenuSubButton
                              onClick={() => {
                                setLocation(`/element-api-explorer?app=${encodeURIComponent(appStatus.application.id)}&showAppInfo=true`);
                              }}
                              isActive={
                                location.includes('/element-api-explorer') &&
                                location.includes(`app=${encodeURIComponent(appStatus.application.id)}`) &&
                                location.includes('showAppInfo=true')
                              }
                              data-testid={`link-app-info-${appStatus.application?.name?.toLowerCase().replace(/\s+/g, '-') || 'unknown'}`}
                            >
                              <Icons.Info className="w-3 h-3" />
                              <span className="truncate">View Application Info</span>
                            </SidebarMenuSubButton>
                          </SidebarMenuSubItem>
                        )}
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

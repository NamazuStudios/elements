import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import * as Icons from 'lucide-react';
import { usePlugins } from '@/contexts/PluginContext';

interface InstalledElementsSidebarProps {
  location: string;
  setLocation: (path: string) => void;
}

export function InstalledElementsSidebar({ location, setLocation }: InstalledElementsSidebarProps) {
  const { plugins, isLoading: pluginsLoading } = usePlugins();

  return (
    <>
    <Collapsible defaultOpen className="group/collapsible">
      <SidebarGroup>
        <SidebarGroupLabel asChild>
          <CollapsibleTrigger className="text-xs uppercase tracking-wider hover-elevate">
            Explorer
            <Icons.ChevronDown className="ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180" />
          </CollapsibleTrigger>
        </SidebarGroupLabel>
        <CollapsibleContent>
          <SidebarGroupContent>
            <SidebarMenu>
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

              <SidebarMenuItem>
                <SidebarMenuButton
                  onClick={() => setLocation('/installed-elements')}
                  isActive={location.startsWith('/installed-elements') || location.startsWith('/element-api-explorer')}
                  data-testid="link-element-apis"
                >
                  <Icons.Package className="w-4 h-4" />
                  <span>Element APIs</span>
                </SidebarMenuButton>
              </SidebarMenuItem>

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
            </SidebarMenu>
          </SidebarGroupContent>
        </CollapsibleContent>
      </SidebarGroup>
    </Collapsible>

    <Collapsible defaultOpen className="group/collapsible-mgmt">
      <SidebarGroup>
        <SidebarGroupLabel asChild>
          <CollapsibleTrigger className="text-xs uppercase tracking-wider hover-elevate">
            Element Management
            <Icons.ChevronDown className="ml-auto transition-transform group-data-[state=open]/collapsible-mgmt:rotate-180" />
          </CollapsibleTrigger>
        </SidebarGroupLabel>
        <CollapsibleContent>
          <SidebarGroupContent>
            <SidebarMenu>
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

              <SidebarMenuItem>
                <SidebarMenuButton
                  onClick={() => setLocation('/containers')}
                  isActive={location === '/containers'}
                  data-testid="link-containers"
                >
                  <Icons.Container className="w-4 h-4" />
                  <span>Containers</span>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton
                  onClick={() => setLocation('/runtimes')}
                  isActive={location === '/runtimes'}
                  data-testid="link-runtimes"
                >
                  <Icons.Cpu className="w-4 h-4" />
                  <span>Runtimes</span>
                </SidebarMenuButton>
              </SidebarMenuItem>

              <SidebarMenuItem>
                <SidebarMenuButton
                  onClick={() => setLocation('/elm-inspector')}
                  isActive={location === '/elm-inspector'}
                  data-testid="link-elm-inspector"
                >
                  <Icons.ScanSearch className="w-4 h-4" />
                  <span>ELM Inspector</span>
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          </SidebarGroupContent>
        </CollapsibleContent>
      </SidebarGroup>
    </Collapsible>

    {pluginsLoading && (
      <div className="px-4 py-2 text-xs text-muted-foreground flex items-center gap-2">
        <div className="w-3 h-3 border-2 border-primary border-t-transparent rounded-full animate-spin" />
        Discovering plugins...
      </div>
    )}

    {plugins.length > 0 && (
      <Collapsible defaultOpen className="group/collapsible-plugins">
        <SidebarGroup>
          <SidebarGroupLabel asChild>
            <CollapsibleTrigger className="text-xs uppercase tracking-wider hover-elevate">
              Element Plugins
              <Icons.ChevronDown className="ml-auto transition-transform group-data-[state=open]/collapsible-plugins:rotate-180" />
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                {plugins.map(plugin => {
                  const IconComponent = (Icons as Record<string, any>)[plugin.icon] || Icons.Package;
                  return (
                    <SidebarMenuItem key={plugin.route}>
                      <SidebarMenuButton
                        onClick={() => setLocation(`/plugin/${plugin.route}`)}
                        isActive={location.startsWith(`/plugin/${plugin.route}`)}
                      >
                        <IconComponent className="w-4 h-4" />
                        <span>{plugin.label}</span>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  );
                })}
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>
    )}
    </>
  );
}

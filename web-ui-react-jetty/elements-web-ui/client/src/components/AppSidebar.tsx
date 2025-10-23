import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import * as Icons from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useResources } from '@/contexts/ResourceContext';
import { useLocation } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import logoPath from '@assets/elements-logo-square (1)_1760052619243.png';
import { InstalledElementsSidebar } from './InstalledElementsSidebar';

const iconMap: Record<string, any> = {
  Shield: Icons.Shield,
  Users: Icons.Users,
  User: Icons.User,
  Package: Icons.Package,
  ShoppingCart: Icons.ShoppingCart,
  Trophy: Icons.Trophy,
  Gamepad2: Icons.Gamepad2,
  Target: Icons.Target,
  Bell: Icons.Bell,
  Settings: Icons.Settings,
  AppWindow: Icons.AppWindow,
  Database: Icons.Database,
  LayoutDashboard: Icons.LayoutDashboard,
  Calendar: Icons.Calendar,
  KeyRound: Icons.KeyRound,
  Lock: Icons.Lock,
  FileCode: Icons.FileCode,
  Vault: Icons.Vault,
  Boxes: Icons.Boxes,
  Box: Icons.Box,
  FileJson: Icons.FileJson,
  Key: Icons.Key,
  HardDrive: Icons.HardDrive,
};

export function AppSidebar() {
  const { username, userLevel, logout, isAuthenticated } = useAuth();
  const { resources } = useResources();
  const [location, setLocation] = useLocation();

  // Fetch Elements version when authenticated
  const { data: versionData } = useQuery<{ version: string }>({
    queryKey: ['/api/proxy/api/rest/version'],
    enabled: isAuthenticated, // Only run when authenticated
    staleTime: Infinity, // Version doesn't change during session
  });

  const dashboardItems = [
    { title: 'Dashboard', icon: 'LayoutDashboard', path: '/dashboard' },
  ];

  // Group resources by category
  const groupedResources = resources.reduce((acc, resource) => {
    if (!acc[resource.category]) {
      acc[resource.category] = [];
    }
    acc[resource.category].push({
      title: resource.name,
      icon: resource.icon,
      path: `/resource/${resource.name.toLowerCase().replace(/\s+/g, '-')}`,
    });
    return acc;
  }, {} as Record<string, Array<{ title: string; icon: string; path: string }>>);

  const categoryOrder = ['Accounts', 'Game', 'Auth', 'Metadata', 'Web3', 'Other'];

  return (
      <Sidebar>
        <SidebarHeader className="border-b border-sidebar-border p-4">
          <div className="flex items-center gap-2">
            <div className="flex items-center justify-center w-8 h-8">
              <img src={logoPath} alt="Elements" className="w-full h-full" />
            </div>
            <div>
              <h2 className="text-sm font-semibold">Namazu Elements</h2>
              <p className="text-xs text-muted-foreground">Management Portal</p>
            </div>
          </div>
        </SidebarHeader>

        <SidebarContent>
          {/* Dashboard - at top level */}
          <SidebarGroup>
            <SidebarGroupContent>
              <SidebarMenu>
                {dashboardItems.map((item) => {
                  const IconComponent = iconMap[item.icon] || Icons.FileQuestion;
                  return (
                      <SidebarMenuItem key={item.path}>
                        <SidebarMenuButton
                            onClick={() => setLocation(item.path)}
                            isActive={location === item.path}
                            data-testid={`link-${item.title.toLowerCase().replace(/\s+/g, '-')}`}
                        >
                          <IconComponent className="w-4 h-4" />
                          <span>{item.title}</span>
                        </SidebarMenuButton>
                      </SidebarMenuItem>
                  );
                })}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>

          {/* Core Elements - collapsible group */}
          <Collapsible defaultOpen className="group/collapsible">
            <SidebarGroup>
              <SidebarGroupLabel asChild>
                <CollapsibleTrigger className="text-xs uppercase tracking-wider hover-elevate">
                  Core Elements
                  <Icons.ChevronDown className="ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180" />
                </CollapsibleTrigger>
              </SidebarGroupLabel>
              <CollapsibleContent>
                <SidebarGroupContent>
                  {/* Accounts items - directly in Core Elements */}
                  {groupedResources['Accounts'] && (
                      <Collapsible defaultOpen={false} className="group/subcollapsible mt-2">
                        <div className="px-2">
                          <CollapsibleTrigger className="flex w-full items-center justify-between text-xs uppercase tracking-wider text-sidebar-foreground/70 hover-elevate rounded-md px-2 py-1">
                            Accounts
                            <Icons.ChevronDown className="h-4 w-4 transition-transform group-data-[state=open]/subcollapsible:rotate-180" />
                          </CollapsibleTrigger>
                        </div>
                        <CollapsibleContent>
                          <SidebarMenu className="mt-1">
                            {groupedResources['Accounts'].map((item) => {
                              const IconComponent = iconMap[item.icon] || Icons.Database;
                              return (
                                  <SidebarMenuItem key={item.path}>
                                    <SidebarMenuButton
                                        onClick={() => setLocation(item.path)}
                                        isActive={location === item.path}
                                        data-testid={`link-${item.title.toLowerCase().replace(/\s+/g, '-')}`}
                                    >
                                      <IconComponent className="w-4 h-4" />
                                      <span>{item.title}</span>
                                    </SidebarMenuButton>
                                  </SidebarMenuItem>
                              );
                            })}
                          </SidebarMenu>
                        </CollapsibleContent>
                      </Collapsible>
                  )}

                  {/* Other Resource Categories - as subcategories */}
                  {categoryOrder.filter(cat => cat !== 'Accounts').map((category) => {
                    const items = groupedResources[category];
                    if (!items || items.length === 0) return null;

                    return (
                        <Collapsible key={category} defaultOpen={false} className="group/subcollapsible mt-4">
                          <div className="px-2">
                            <CollapsibleTrigger className="flex w-full items-center justify-between text-xs uppercase tracking-wider text-sidebar-foreground/70 hover-elevate rounded-md px-2 py-1">
                              {category}
                              <Icons.ChevronDown className="h-4 w-4 transition-transform group-data-[state=open]/subcollapsible:rotate-180" />
                            </CollapsibleTrigger>
                          </div>
                          <CollapsibleContent>
                            <SidebarMenu className="mt-1">
                              {items.map((item) => {
                                const IconComponent = iconMap[item.icon] || Icons.Database;
                                return (
                                    <SidebarMenuItem key={item.path}>
                                      <SidebarMenuButton
                                          onClick={() => setLocation(item.path)}
                                          isActive={location === item.path}
                                          data-testid={`link-${item.title.toLowerCase().replace(/\s+/g, '-')}`}
                                      >
                                        <IconComponent className="w-4 h-4" />
                                        <span>{item.title}</span>
                                      </SidebarMenuButton>
                                    </SidebarMenuItem>
                                );
                              })}
                            </SidebarMenu>
                          </CollapsibleContent>
                        </Collapsible>
                    );
                  })}
                </SidebarGroupContent>
              </CollapsibleContent>
            </SidebarGroup>
          </Collapsible>

          {/* API Explorer - collapsible group */}
          <InstalledElementsSidebar location={location} setLocation={setLocation} />
        </SidebarContent>

        <SidebarFooter className="border-t border-sidebar-border p-4">
          <div className="space-y-1.5">
            <div className="flex items-center gap-3">
              <div className="flex items-center justify-center w-8 h-8 rounded-full bg-primary/10 text-primary font-medium text-sm">
                {username?.charAt(0).toUpperCase()}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate" data-testid="text-username">
                  {username}
                </p>
                <Badge variant="outline" className="text-xs mt-1">
                  <Icons.Shield className="w-3 h-3 mr-1" />
                  {userLevel}
                </Badge>
              </div>
            </div>
            <Button
                variant="ghost"
                className="w-full justify-start"
                onClick={() => setLocation('/settings')}
                data-testid="button-settings"
            >
              <Icons.Settings className="w-4 h-4 mr-2" />
              Settings
            </Button>
            <Button
                variant="ghost"
                className="w-full justify-start"
                onClick={logout}
                data-testid="button-logout"
            >
              <Icons.LogOut className="w-4 h-4 mr-2" />
              Logout
            </Button>
            <div className="pt-1.5 border-t border-sidebar-border">
              <p className="text-xs text-muted-foreground text-center" data-testid="text-version">
                Elements {versionData?.version ? `v${versionData.version}` : '(loading...)'}
              </p>
            </div>
          </div>
        </SidebarFooter>
      </Sidebar>
  );
}

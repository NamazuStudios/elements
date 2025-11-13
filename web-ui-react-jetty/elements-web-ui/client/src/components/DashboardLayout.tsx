import { SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar';
import { AppSidebar } from './AppSidebar';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/contexts/AuthContext';
import { useLocation } from 'wouter';
import { useState } from 'react';
import * as Icons from 'lucide-react';

interface DashboardLayoutProps {
  children: React.ReactNode;
}

export default function DashboardLayout({ children }: DashboardLayoutProps) {
  const { username, logout } = useAuth();
  const [, setLocation] = useLocation();
  
  // Track which category groups are open - lifted to persist across navigation
  const [openGroups, setOpenGroups] = useState<Record<string, boolean>>({});

  const style = {
    '--sidebar-width': '16rem',
  };

  return (
    <SidebarProvider defaultOpen style={style as React.CSSProperties}>
      <div className="flex h-screen w-full">
        <AppSidebar openGroups={openGroups} setOpenGroups={setOpenGroups} />
        <div className="flex flex-col flex-1 overflow-hidden">
          <header className="flex items-center gap-4 border-b px-6 py-3">
            <SidebarTrigger data-testid="button-sidebar-toggle" />
            <div className="flex-1" />
            <div className="flex items-center gap-2">
              <div className="flex items-center gap-2 px-3 py-1.5 rounded-md bg-muted">
                <div className="flex items-center justify-center w-7 h-7 rounded-full bg-primary/10 text-primary font-medium text-xs">
                  {username?.charAt(0).toUpperCase()}
                </div>
                <span className="text-sm font-medium" data-testid="text-username">{username}</span>
              </div>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => setLocation('/settings')}
                data-testid="button-settings"
              >
                <Icons.Settings className="w-4 h-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                onClick={logout}
                data-testid="button-logout"
              >
                <Icons.LogOut className="w-4 h-4" />
              </Button>
            </div>
          </header>
          <main className="flex-1 overflow-auto p-6">
            {children}
          </main>
        </div>
      </div>
    </SidebarProvider>
  );
}

import { SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar';
import { AppSidebar } from './AppSidebar';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/contexts/AuthContext';
import { useLocation } from 'wouter';
import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import * as Icons from 'lucide-react';
import { apiRequest } from '@/lib/queryClient';

interface DashboardLayoutProps {
  children: React.ReactNode;
}

function totpBadgeDismissedKey(username: string | null) {
  return `totp-nudge-dismissed:${username ?? 'unknown'}`;
}

export default function DashboardLayout({ children }: DashboardLayoutProps) {
  const { username, logout } = useAuth();
  const [location, setLocation] = useLocation();

  // Track which category groups are open - lifted to persist across navigation
  const [openGroups, setOpenGroups] = useState<Record<string, boolean>>({});

  // Nudge badge: shown on the Settings icon when TOTP is available system-wide but this account hasn't
  // enrolled yet. Dismissed (for this account) the first time they visit Settings, regardless of whether
  // they actually enroll -- it's a one-time nudge, not a persistent "you're insecure" warning.
  const [badgeDismissed, setBadgeDismissed] = useState(
    () => localStorage.getItem(totpBadgeDismissedKey(username)) === 'true'
  );

  const { data: totpConfig } = useQuery<{ enabled: boolean }>({
    queryKey: ['/api/rest/totp_configuration'],
    queryFn: async () => {
      const response = await apiRequest('GET', '/api/rest/totp_configuration');
      return await response.json();
    },
    staleTime: 60_000,
  });

  const { data: isEnrolled } = useQuery<boolean>({
    queryKey: ['/api/rest/totp'],
    queryFn: async () => {
      const response = await apiRequest('GET', '/api/rest/totp');
      return await response.json();
    },
    staleTime: 60_000,
  });

  useEffect(() => {
    if (location === '/settings' && !badgeDismissed) {
      localStorage.setItem(totpBadgeDismissedKey(username), 'true');
      setBadgeDismissed(true);
    }
  }, [location, badgeDismissed, username]);

  const showTotpBadge = !!totpConfig?.enabled && isEnrolled === false && !badgeDismissed;

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
                className="relative"
                onClick={() => setLocation('/settings')}
                data-testid="button-settings"
              >
                <Icons.Settings className="w-4 h-4" />
                {showTotpBadge && (
                  <span
                    className="absolute -top-0.5 -right-0.5 flex h-3.5 w-3.5 items-center justify-center rounded-full bg-destructive text-[9px] font-bold text-destructive-foreground"
                    data-testid="badge-totp-nudge"
                  >
                    !
                  </span>
                )}
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

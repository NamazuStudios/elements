import { AppSidebar } from '../AppSidebar';
import { SidebarProvider } from '@/components/ui/sidebar';
import { AuthProvider } from '@/contexts/AuthContext';

export default function AppSidebarExample() {
  const style = {
    '--sidebar-width': '16rem',
  };

  return (
    <AuthProvider>
      <SidebarProvider style={style as React.CSSProperties}>
        <div className="flex h-screen w-full">
          <AppSidebar />
          <div className="flex-1 p-8 bg-background">
            <h1 className="text-2xl font-semibold">Main Content Area</h1>
            <p className="text-muted-foreground mt-2">This is where the page content would appear</p>
          </div>
        </div>
      </SidebarProvider>
    </AuthProvider>
  );
}

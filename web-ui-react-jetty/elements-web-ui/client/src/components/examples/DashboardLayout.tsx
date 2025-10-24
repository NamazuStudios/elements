import DashboardLayout from '../DashboardLayout';
import { AuthProvider } from '@/contexts/AuthContext';

export default function DashboardLayoutExample() {
  return (
    <AuthProvider>
      <DashboardLayout>
        <div className="space-y-6">
          <div>
            <h1 className="text-2xl font-semibold">Page Title</h1>
            <p className="text-muted-foreground mt-1">Page description goes here</p>
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <div className="border rounded-lg p-6">
              <h3 className="font-medium mb-2">Content Block 1</h3>
              <p className="text-sm text-muted-foreground">This is where content would appear</p>
            </div>
            <div className="border rounded-lg p-6">
              <h3 className="font-medium mb-2">Content Block 2</h3>
              <p className="text-sm text-muted-foreground">This is where content would appear</p>
            </div>
          </div>
        </div>
      </DashboardLayout>
    </AuthProvider>
  );
}

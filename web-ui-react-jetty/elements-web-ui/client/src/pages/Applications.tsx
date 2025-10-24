import { useState } from 'react';
import ResourceTable from '@/components/ResourceTable';
import ResourceFormDialog from '@/components/ResourceFormDialog';
import { Badge } from '@/components/ui/badge';
import { useToast } from '@/hooks/use-toast';

//todo: remove mock functionality
const mockApplications = [
  { id: '1', name: 'Mobile Game Alpha', apiKey: 'app_key_abc123', environment: 'production', status: 'active' },
  { id: '2', name: 'Web Portal Beta', apiKey: 'app_key_def456', environment: 'staging', status: 'active' },
  { id: '3', name: 'Console Game', apiKey: 'app_key_ghi789', environment: 'development', status: 'inactive' },
];

export default function Applications() {
  const [applications, setApplications] = useState(mockApplications);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingApp, setEditingApp] = useState<any>(null);
  const { toast } = useToast();

  const columns = [
    { key: 'name', label: 'Application Name' },
    {
      key: 'apiKey',
      label: 'API Key',
      render: (item: any) => (
        <code className="text-xs font-mono bg-muted px-2 py-1 rounded">
          {item.apiKey}
        </code>
      ),
    },
    {
      key: 'environment',
      label: 'Environment',
      render: (item: any) => (
        <Badge
          variant={
            item.environment === 'production'
              ? 'default'
              : item.environment === 'staging'
              ? 'secondary'
              : 'outline'
          }
        >
          {item.environment}
        </Badge>
      ),
    },
    {
      key: 'status',
      label: 'Status',
      render: (item: any) => (
        <Badge variant={item.status === 'active' ? 'default' : 'outline'}>
          {item.status}
        </Badge>
      ),
    },
  ];

  const formFields = [
    { name: 'name', label: 'Application Name', type: 'text' as const, placeholder: 'My Game', required: true },
    {
      name: 'environment',
      label: 'Environment',
      type: 'select' as const,
      options: [
        { value: 'development', label: 'Development' },
        { value: 'staging', label: 'Staging' },
        { value: 'production', label: 'Production' },
      ],
      required: true,
    },
    {
      name: 'status',
      label: 'Status',
      type: 'select' as const,
      options: [
        { value: 'active', label: 'Active' },
        { value: 'inactive', label: 'Inactive' },
      ],
      required: true,
    },
  ];

  const handleCreate = () => {
    setEditingApp(null);
    setIsDialogOpen(true);
  };

  const handleEdit = (app: any) => {
    setEditingApp(app);
    setIsDialogOpen(true);
  };

  const handleDelete = (app: any) => {
    //todo: remove mock functionality
    setApplications(applications.filter((a) => a.id !== app.id));
    toast({
      title: 'Application deleted',
      description: `${app.name} has been removed`,
    });
  };

  const handleSubmit = (data: any) => {
    //todo: remove mock functionality
    if (editingApp) {
      setApplications(applications.map((a) => (a.id === editingApp.id ? { ...a, ...data } : a)));
      toast({
        title: 'Application updated',
        description: `${data.name} has been updated`,
      });
    } else {
      const newApp = {
        ...data,
        id: String(applications.length + 1),
        apiKey: `app_key_${Math.random().toString(36).substr(2, 9)}`,
      };
      setApplications([...applications, newApp]);
      toast({
        title: 'Application created',
        description: `${data.name} has been added`,
      });
    }
    setIsDialogOpen(false);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Applications</h1>
        <p className="text-muted-foreground mt-1">Manage registered applications and API keys</p>
      </div>

      <ResourceTable
        title="All Applications"
        data={applications}
        columns={columns}
        onEdit={handleEdit}
        onDelete={handleDelete}
        onCreate={handleCreate}
        searchPlaceholder="Search applications..."
      />

      <ResourceFormDialog
        open={isDialogOpen}
        onOpenChange={setIsDialogOpen}
        title={editingApp ? 'Edit Application' : 'Create Application'}
        description={editingApp ? 'Update application settings' : 'Register a new application'}
        fields={formFields}
        initialData={editingApp || {}}
        onSubmit={handleSubmit}
      />
    </div>
  );
}

import { useState } from 'react';
import ResourceTable from '@/components/ResourceTable';
import ResourceFormDialog from '@/components/ResourceFormDialog';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Package } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { InventoryViewer } from '@/components/InventoryViewer';

//todo: remove mock functionality
const mockUsers = [
  { id: '1', username: 'john_doe', email: 'john@example.com', level: 'USER', status: 'active' },
  { id: '2', username: 'jane_smith', email: 'jane@example.com', level: 'ADMIN', status: 'active' },
  { id: '3', username: 'bob_wilson', email: 'bob@example.com', level: 'SUPERUSER', status: 'active' },
  { id: '4', username: 'alice_brown', email: 'alice@example.com', level: 'USER', status: 'inactive' },
  { id: '5', username: 'charlie_davis', email: 'charlie@example.com', level: 'USER', status: 'active' },
];

export default function Users() {
  const [users, setUsers] = useState(mockUsers);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<any>(null);
  const [inventoryOpen, setInventoryOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<any>(null);
  const { toast } = useToast();

  const handleViewInventory = (user: any) => {
    setSelectedUser(user);
    setInventoryOpen(true);
  };

  const columns = [
    { key: 'username', label: 'Username' },
    { key: 'email', label: 'Email' },
    {
      key: 'level',
      label: 'Level',
      render: (item: any) => (
        <Badge variant={item.level === 'SUPERUSER' ? 'default' : 'secondary'}>
          {item.level}
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
    {
      key: 'inventory',
      label: 'Inventory',
      render: (item: any) => (
        <Button
          variant="outline"
          size="sm"
          onClick={() => handleViewInventory(item)}
          data-testid={`button-view-inventory-${item.id}`}
        >
          <Package className="w-4 h-4 mr-2" />
          View
        </Button>
      ),
    },
  ];

  const formFields = [
    { name: 'username', label: 'Username', type: 'text' as const, placeholder: 'Enter username', required: true },
    { name: 'email', label: 'Email', type: 'email' as const, placeholder: 'user@example.com', required: true },
    {
      name: 'password',
      label: 'Password',
      type: 'password' as const,
      placeholder: 'Enter password',
      required: !editingUser,
      validate: (value: string) => {
        if (!editingUser && !value) {
          return 'Password is required';
        }
        if (value && value.length < 8) {
          return 'Password must be at least 8 characters';
        }
        return undefined;
      },
    },
    {
      name: 'confirmPassword',
      label: 'Confirm Password',
      type: 'password' as const,
      placeholder: 'Confirm password',
      required: !editingUser,
      validate: (value: string, formData: Record<string, any>) => {
        if (!editingUser && !value) {
          return 'Please confirm your password';
        }
        if (value && value !== formData.password) {
          return 'Passwords do not match';
        }
        return undefined;
      },
    },
    {
      name: 'level',
      label: 'User Level',
      type: 'select' as const,
      options: [
        { value: 'USER', label: 'User' },
        { value: 'ADMIN', label: 'Admin' },
        { value: 'SUPERUSER', label: 'Superuser' },
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
    setEditingUser(null);
    setIsDialogOpen(true);
  };

  const handleEdit = (user: any) => {
    setEditingUser(user);
    setIsDialogOpen(true);
  };

  const handleDelete = (user: any) => {
    //todo: remove mock functionality
    setUsers(users.filter((u) => u.id !== user.id));
    toast({
      title: 'User deleted',
      description: `${user.username} has been removed`,
    });
  };

  const handleSubmit = (data: any) => {
    //todo: remove mock functionality
    if (editingUser) {
      setUsers(users.map((u) => (u.id === editingUser.id ? { ...u, ...data } : u)));
      toast({
        title: 'User updated',
        description: `${data.username} has been updated`,
      });
    } else {
      const newUser = { ...data, id: String(users.length + 1) };
      setUsers([...users, newUser]);
      toast({
        title: 'User created',
        description: `${data.username} has been added`,
      });
    }
    setIsDialogOpen(false);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Users</h1>
        <p className="text-muted-foreground mt-1">Manage user accounts and permissions</p>
      </div>

      <ResourceTable
        title="All Users"
        data={users}
        columns={columns}
        onEdit={handleEdit}
        onDelete={handleDelete}
        onCreate={handleCreate}
        searchPlaceholder="Search users..."
      />

      <ResourceFormDialog
        open={isDialogOpen}
        onOpenChange={setIsDialogOpen}
        title={editingUser ? 'Edit User' : 'Create User'}
        description={editingUser ? 'Update user information' : 'Add a new user to the system'}
        fields={formFields}
        initialData={editingUser || {}}
        onSubmit={handleSubmit}
      />

      {/* Inventory Viewer Dialog */}
      {selectedUser && (
        <Dialog open={inventoryOpen} onOpenChange={(open) => {
          setInventoryOpen(open);
          if (!open) setSelectedUser(null);
        }}>
          <DialogContent className="max-w-6xl max-h-[90vh]">
            <DialogHeader>
              <DialogTitle>Inventory for {selectedUser.username}</DialogTitle>
            </DialogHeader>
            <InventoryViewer userId={selectedUser.id} username={selectedUser.username} />
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}

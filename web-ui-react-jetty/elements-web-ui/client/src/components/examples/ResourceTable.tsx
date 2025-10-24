import ResourceTable from '../ResourceTable';
import { Badge } from '@/components/ui/badge';

//todo: remove mock functionality
const mockUsers = [
  { id: '1', username: 'john_doe', email: 'john@example.com', level: 'USER', status: 'active' },
  { id: '2', username: 'jane_smith', email: 'jane@example.com', level: 'ADMIN', status: 'active' },
  { id: '3', username: 'bob_wilson', email: 'bob@example.com', level: 'SUPERUSER', status: 'active' },
  { id: '4', username: 'alice_brown', email: 'alice@example.com', level: 'USER', status: 'inactive' },
];

export default function ResourceTableExample() {
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
  ];

  return (
    <div className="p-6 bg-background">
      <ResourceTable
        title="Users"
        data={mockUsers}
        columns={columns}
        onEdit={(item) => console.log('Edit:', item)}
        onDelete={(item) => console.log('Delete:', item)}
        onCreate={() => console.log('Create new user')}
        searchPlaceholder="Search users..."
      />
    </div>
  );
}

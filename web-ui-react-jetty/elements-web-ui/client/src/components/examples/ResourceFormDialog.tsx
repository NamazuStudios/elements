import { useState } from 'react';
import ResourceFormDialog from '../ResourceFormDialog';
import { Button } from '@/components/ui/button';

export default function ResourceFormDialogExample() {
  const [open, setOpen] = useState(false);

  const fields = [
    { name: 'username', label: 'Username', type: 'text' as const, placeholder: 'Enter username', required: true },
    { name: 'email', label: 'Email', type: 'email' as const, placeholder: 'user@example.com', required: true },
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
    { name: 'bio', label: 'Bio', type: 'textarea' as const, placeholder: 'Tell us about yourself...' },
  ];

  return (
    <div className="p-6 bg-background">
      <Button onClick={() => setOpen(true)}>Open Form Dialog</Button>
      <ResourceFormDialog
        open={open}
        onOpenChange={setOpen}
        title="Create User"
        description="Add a new user to the system"
        fields={fields}
        onSubmit={(data) => {
          console.log('Form submitted:', data);
          setOpen(false);
        }}
      />
    </div>
  );
}
